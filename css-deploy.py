#!/usr/bin/env python3
#
#  Copyright (c) 2019 Johannes C. Kazantzidis
#
#  The program is free software: You can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published
#  by the Free Software Foundation.
#
#  This program is provided as is, WITHOUT ANY WARRANTY. See the GNU General
#  Public License for more details.
#
#  You should have received a copy of the GNU General Public License along with
#  this program. If not, see https://www.gnu.org/licenses/gpl.txt

__version__ = '0.2.1'
__author__ = 'Johannes C. Kazantzidis'

import argparse
import datetime
import distutils
import fileinput
from getpass import getpass
import html
import json
import os
import platform
import re
import subprocess
import sys

import requests

def checkBranch():
    """Check current git branch.

    User must be on master branch to deploy. If this is not the case,
    the script will notify user and abort.
    """
    inform("Verifying git branch")

    cmd = "git branch | grep \* | cut -d ' ' -f2"

    branch = subprocess.check_output(cmd, shell=True).decode("utf8").split()[0]

    if not "master" in branch:
        msg = "You are on branch '{}'." .format(branch)
        msg += " Please checkout master branch before running this script."
        print(msg)
        sys.exit(1)

def checkBranch():
    """Check current git branch.

    User must be on master branch to deploy. If this is not the case,
    the script will notify user and abort.
    """
    cmd = "git branch | grep \* | cut -d ' ' -f2"

    branch = subprocess.check_output(cmd, shell=True).decode("utf8").split()[0]

    if not "master" in branch:
        print("You are on branch '{}'. " \
                "Please checkout master branch before running this script."
                .format(branch))
        sys.exit(1)

def checkJiraRelease(version, auth):
    """Check if version is released in Jira.

    Args:
        version: Full CSS version number to be released, e.g. 4.6.1.12
        auth: Username and password pair for Jira
    """
    inform("Checking if CSS version {} is released in Jira" .format(version))

    # REST url for the release version
    url = 'https://jira.esss.lu.se/rest/api/2/search?jql=project=CSSTUDIO AND fixVersion="ESS CS-Studio '+version+'"'

    headers = {"Content-Type":"application/json"}
    response = requests.get(url, auth=auth, headers=headers)
    data = response.json()

    if response.status_code == 400:
        msg = "Could not find Jira Release of version {}." .format(version)
        msg += "Please go to Jira Kanban Board and press 'Release...' "
        msg += "before running this script.\nAborting"
        print(msg)
        sys.exit(1)

def checkJavaHome():
    """Check if user has `JAVA_HOME` environment variable set.

    The variable needed for the `prepare-release.sh` and
    `prepare-next-release.sh` scripts
    """
    inform("Checking Java environment")

    if "JAVA_HOME" in os.environ:
        return # env var found, all good
    elif platform.system() == "Linux":
        print("You don't seem to have a path in the JAVA_HOME variable")
        suggestion_cmd = "dirname $(readlink -f $(which java))"
    elif platform.system() == "Darwin":
        suggestion_cmd = "dirname $(readlink $(which java))"

    suggestion = subprocess.check_output(suggestion_cmd,
                                             shell=True).decode("utf8")

    print("Put the following your `.profile` file or `.bashrc` file:\n{}"
                  .format(suggestion))

    # Ask if user wants to try to continue even though `JAVA_HOME` was not found
    if diagYes("Would you still like to proceed? [y/n]"):
        return
    else:
        sys.exit(0)

def checkVersion(user_input):
    """Checks the CSS version input against artifactory.

    Grabs latest CSS version number from
    https://artifactory.esss.lu.se/artifactory/CS-Studio/production/ and
    increments nano version (i.e. last number) by one. If the resulting
    number differs from user input, the user is prompted for
    verification to continue anyway.

    Args:
        user_input: Version number as string, input by user.
    """
    inform("Checking CSS version against artifactory")

    url = "https://artifactory.esss.lu.se/artifactory/CS-Studio/production/"
    pattern = re.compile("[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+") # version pattern
    params = {"q": "ISOW7841FDWER"}
    headers = {"User-Agent": "Mozilla/5"}
    r = requests.get(url, params=params, headers=headers)

    versions = set(pattern.findall(r.text)) # A set containing all versions
    latest = ""
    latest_sum = 0

    # Find latest version
    for v in versions:
        split = v.split(".")
        factor = 1
        version_sum = 0
        for n in reversed(split):
            version_sum += int(n)*factor
            factor*=100
        if version_sum > latest_sum:
            latest_sum = version_sum
            latest = v

    # New version suggestion is latest version + 1 (increment nano version)
    new_version = ".".join([
        ".".join(latest.split(".")[:-1]),
        str(int(latest.split(".")[-1])+1)
        ])

    # If user input version is not same as next nano version, prompt user
    if new_version != user_input:
        print("Suggested version number is {}" .format(new_version))
        if diagYes("Are you sure you wish to use {}? [y/n]" .format(user_input)):
            return
        else:
            print("Aborting")
            sys.exit()

def prepareRelease(path, version, ce_version):
    """Run `prepare-release.sh`.

    `prepare-release.sh` is a community developed script for creating
    new splash screen, change 'about' dialog, change Ansible reference
    file, update plugin versions, update product versions in product
    files, update product versions in master POM file and
    commit-tag-push changes.

    Args:
        path: Path to `prepare-release.sh`.
        version: CSS release version to prepare.
        ce_version: CSS CE version of which the CSS release is based on.
    """
    inform("Running prepare-release.sh")

    url = "https://jira.esss.lu.se/projects/CSSTUDIO/versions/23001"
    cmd = 'bash {}prepare-release.sh {} "{}" ' .format(path, version, url)
    cmd += '"PLACEHOLDER-TEXT-FOR-HTML-NOTES</li><li>'
    cmd += 'Based on CS-Studio CE {}-SNAPSHOT" false' .format(ce_version)

    try:
        subprocess.check_call(cmd, shell=True)
    except subprocess.CalledProcessError as e:
        print("\nOops, something went wrong when running 'prepare-release.sh'")
        print("Aborting")
        sys.exit(1)

def updateChangelog(dir_path, notes):
    """Update changelog.

    The prepare-release.sh script has some `sed` calls on the changelog
    html code that do not seem to work well on mac. Therefore, a
    placeholder text is put in the changelog in its stead and is then
    replaced by this method.

    Args:
        dir_path: Path to changelog.
        notes: Notes to replace the placeholder text with.
    """
    inform("Updating changelog")

    pattern = "PLACEHOLDER-TEXT-FOR-HTML-NOTES"
    replacement = notes
    path = "{}/plugins/se.ess.ics.csstudio.startup.intro/html/changelog.html" .format(dir_path)
    patReplace(path, pattern, notes)

def prepareNextRelease(version):
    """Run `prepare-next-release.sh`.

    `prepare-next-release.sh` is a community developed script for
    preparing splash screen, change 'about' dialog, change Ansible
    reference file, updating plugin versions, update product versions in
    master POM file, commit version and push changes.

    Args:
        version: CSS version of next release.
    """
    inform("Running prepare-next-release.sh")

    path = os.path.dirname(os.path.abspath(__file__))+"/"

    cmd = "bash {}prepare-next-release.sh {} false" .format(path, version)

    subprocess.check_call(cmd, shell=True)

def getNextVersion(version):
    """Get next CSS version based on the current deployment version.

    Deduce next version by incrementing nano version, and verify with
    user. If it is not the desired next version, allow for user to
    manually set next version.

    Args:
        version: CSS version of this release

    Returns:
        next_version: Next CSS version
    """
    # Determine next version by incrementing nano version of the current release
    next_version = ".".join([
        ".".join(version.split(".")[:-1]),
        str(int(version.split(".")[-1])+1)
        ])

    # Verify version with user
    print("Suggested version number for next release is {}"
              .format(next_version))

    if not diagYes("Is this correct {}? [y/n]" .format(next_version)):
        next_version = input("Enter next version number?")

    return next_version

def patReplace(path, pattern, repl):
    """Replace a pattern in a file.

    Inplace relpacement of text in a file. Original file will be backed
    up with <filename>.bak

    Args:
        path: Path to file in which to replace text.
        pattern: Regular expression pattern to search for.
        repl: Replacement text with which to replace text matching to `pattern`.
    """
    pat = re.compile(pattern)
    with fileinput.FileInput(path, inplace=True) as file:
        for line in file:
            result = re.sub(pat, repl, line)
            print(result, end="")

def updatePom(path, version):
    """Update pom.xml file.

    Update cs-studio major, and minor, version number in pom.xml file.

    Args:
        path: Path to pom.xml file
        version: Full CSS version number to be released, e.g. 4.6.1.12
    """
    inform("Updating POM.xml")

    split = version.split(".")
    majmin = ".".join(split[0:2])
    pattern = "<cs-studio.version>[0-9]+\.[0-9]+</cs-studio.version>"
    replacement = "<cs-studio.version>" + majmin + "</cs-studio.version>"
    patReplace(path, pattern, replacement)

def getChangelogNotes(version, auth):
    """Get notes for changelog from Jira.

    Get notes from Jira via REST interface and format the notes to be
    accepted by the `prepare-release.sh` script (see function
    `prepareRelease` in this file for more info.).

    Args:
        version: Full CSS version number to be released, e.g. 4.6.1.12
        auth: Username and password pair for Jira
    """
    inform("Fetching changelog notes")

    # REST url for issues specific for the release
    url = 'https://jira.esss.lu.se/rest/api/2/search?jql=project=CSSTUDIO AND fixVersion="ESS CS-Studio '+version+'"'

    headers = {"Content-Type":"application/json"}
    response = requests.get(url, auth=auth, headers=headers)
    data = response.json()
    note_list = []
    pattern = re.compile("CSS-CE #[0-9]+")

    # `CSS-CE #XXX` is a merge from the community version. Sort `note_list`
    # with CSS-CE merges first.
    for issue in data["issues"]:
        summary = htmlEscape(issue["fields"]["summary"])
        if list(pattern.findall(summary)):
            note_list.insert(0,"<li>"+summary+"</li>")
        else:
            note_list.append("<li>"+summary+"</li>")

    # Now that the comment_list is sorted, put them into one string to fit the
    # format expected by the prepare-realease.sh script
    notes_str = ""
    for note in note_list:
        notes_str += note

    # The `prepare-release.sh` script will put <li></li> around the note
    # string. Since several notes may be used, <li></li> must be added around
    # each note as above. To satisfy the `prepare-release.sh` script however,
    # the first <li> and the last </li> of `notes_str` must be excluded. One
    # may be tempted to modify the `prepare-release.sh` script instead, though
    # as it is made by, and spread amongst, the community it is decided to be
    # kept intact for the time being.
    formatted_notes = notes_str[4:-5]

    return formatted_notes

def htmlEscape(text):
    """Produce entities within text."""
    html_escape_table = {
        "&": "&amp;",
        '"': "&quot;",
        "'": "&apos;",
        ">": "&gt;",
        "<": "&lt;",
        "`": "&apos;",
        "´": "&apos;",
        "\\": "&bsol;",
        }

    return "".join(html_escape_table.get(c,c) for c in text)

def mergeRepos(path, version):
    """Merge all relevant repositories into production.

    Args:
        path: Path to `merge.sh`.
        version: CSS version for this release.
    """
    inform("Merging git repositories")

    merge_cmd = str("bash {} {}" .format(path, version))

    try:
        subprocess.check_call(merge_cmd, shell=True)
    except subprocess.CalledProcessError as e:
        print("\nAn error occured when running merge.sh\nAborting")
        sys.exit()

def updateConfluenceNotes(css_version, ce_version, notes, auth):
    """Update CSS confluence page's release notes.

    Create a new linked header and add "Compatibility Notes" and
    "Updated Features".

    Args:
        css_version: New CSS release version.
        ce_version: CSS CE version of which the CSS release is based on.
        notes: Notes to be inserted into the changelog.
        auth: Atlassian athentication (username, password) pair.
    """
    inform("Updating Confluence release notes")

    base_url = "https://confluence.esss.lu.se/rest/api/content/"
    page_id = "129630590" # Use 295789998 for the dummy page

    # Pull data from the confluence page
    get_url = base_url + page_id + "?expand=body.view,version"
    get_response = requests.get(get_url, auth=auth)
    data = get_response.json()

    put_url = base_url + page_id
    headers = {"Content-type": "application/json"}

    # Code for generating the TOC
    toc ='<p><ac:structured-macro ac:name="toc" ac:schema-version="1" ' \
      'ac:macro-id="f222cec9-dc7a-413d-abc7-a4710756bec0"><ac:parameter ' \
      'ac:name="maxLevel">2</ac:parameter></ac:structured-macro></p>'

    # Header with link etc. for the new version text
    d = datetime.date.today()
    link_address_date = d.strftime("%d.%m.%Y")
    header_date = d.strftime("%B %d, %Y")
    header_link = '"https://confluence.esss.lu.se/display/CR/' \
      'ESS+CS-Studio+Releases#ESSCS-StudioReleases-Ver.' \
      + css_version + '(' + link_address_date +')"'

    # Create the new section to put on the confluence page
    new_section = '<h2><a href=' + header_link + ' rel="nofollow">Ver. ' \
      + css_version + '</a>    - ' + header_date + \
      '</h2><h3>Compatibility Notes</h3>' \
      '<ul><li>Based on the most recent source code of CS-Studio CE&nbsp;' \
      + ce_version + '-SNAPSHOT.</li></ul><h3>Updated Features</h3><ul><li>' \
      + notes + '</li></ul>'

    # Check if this version has already been put on the confluence page. If so,
    # ask user if they still want to update with the given information. If user
    # chooses 'no', skip the confluence update.
    if "Ver. " + css_version +"" in data["body"]["view"]["value"]:
        dialogMsg = "A header for CSS version {} was found on " \
                               "the confluence page. Are you sure you wish " \
                               "to post your notes? [y/n]" .format(css_version)

        if not diagYes(dialogMsg):
            print("Skipping confluence update")
            return

    # Construct json payload
    payload = {}
    payload["type"] = data["type"]
    payload["title"] = data["title"]
    payload["version"] = {}
    payload["version"]["number"] = data["version"]["number"]+1

    # Remove toc part, we'll make a new toc
    break_string = '"toc\"> </div></p>'
    content = data["body"]["view"]["value"].split(break_string)

    payload["body"] = {}
    payload["body"]["storage"] = {}
    payload["body"]["storage"]["value"] = toc + new_section + content[1]
    representation = data["body"]["view"]["representation"]
    payload["body"]["storage"]["representation"] = representation

    # Post to confluence and verify response
    put_response = requests.put(put_url, data=json.dumps(payload),
                                    auth=auth, headers=headers)

    if put_response.status_code != 200:
        print("Confluence Response Code {}" .format(put_response.status_code))
        print("Aborting")
        sys.exit(1)

def updateConfluenceRelease(css_version, next_version, ce_version, auth):
    """Update CSS confluence page's release notes.

    Create a new linked header and add "Compatibility Notes" and
    "Updated Features".

    Args:
        css_version: New CSS release version.
        ce_version: CSS CE version of which the CSS release is based on.
        auth: Atlassian athentication (username, password) pair.
    """
    inform("Updating Confluence release page")

    base_url = "https://confluence.esss.lu.se/rest/api/content/"
    page_id = "186483068" # Use 295789998 for the dummy page

    # Pull data from the confluence page
    get_url = base_url + page_id + "?expand=body.view,version"
    get_response = requests.get(get_url, auth=auth)
    data = get_response.json()

    # Check if this version has already been put on the confluence page. If so,
    # ask user if they still want to update with the given information. If user
    # chooses 'no', skip the confluence update.
    if "Ver. " + next_version +"" in data["body"]["view"]["value"]:
        msg = "A header for CSS version {} was found on " .format(css_version)
        msg += "the confluence page. Are you sure you wish to update the page? "
        msg += "[y/n]"

        if not diagYes(msg):
            print("Skipping confluence update")
            return

    # Code for generating the TOC
    toc = '<p>&nbsp;</p><p><ac:structured-macro ac:name="toc" ac:schema-' \
      'version="1" ac:macro-id="a3eac23d-3226-4292-a87e-156db912bc91"/></p>'

    # Remove old toc and add new
    break_string = '"toc"> </div></p>'
    toc_stop = str(data["body"]["view"]["value"]).split(break_string)
    result = toc + toc_stop[1]

    # Update Development section
    result = re.sub(css_version, next_version, result)
    what_changed_start = result.split("What Is Changed</h4>")
    what_changed_stop = result.split("Production</h2>")
    result = what_changed_start[0] \
      + "What Is Changed</h4>" \
      + '<ul><li><span style="color: rgb(0,0,0);">Nothing yet</span>.</li>' \
      +"</ul><h2>Production</h2>" \
      + what_changed_stop[1]

    # Get url for the release
    url = 'https://jira.esss.lu.se/rest/api/2/search?jql=project=CSSTUDIO AND fixVersion="ESS CS-Studio '+css_version+'"'
    headers = {"Content-Type":"application/json"}
    response = requests.get(url, auth=auth, headers=headers)
    release_data = response.json()

    if response.status_code == 400:
        print("{} in Jira. Could not fetch changelog notes: Missing Jira " \
                  "release?\nAborting"
                  .format(release_data["errorMessages"][0][:-1]))
        sys.exit(1)

    release_id = release_data["issues"][0]["fields"]["fixVersions"][0]["id"]
    release_url = "https://jira.esss.lu.se/projects/CSSTUDIO/versions/" + release_id

    # Update Production section
    d = datetime.date.today()
    link_address_date = d.strftime("%d.%m.%Y")
    date = d.strftime("%d.%m.%Y")
    production = result.split("Production</h2>")
    result = production[0] \
      + "Production</h2>" \
      + '<h3>Ver. ' + css_version + ' (' + date + ')</h3><ul><li><a rel="nofollow" href="' + release_url + '" class="external-link">Release Details</a></li><li><a href="https://artifactory.esss.lu.se/artifactory/CS-Studio/production/' + css_version + '/cs-studio-ess-' + css_version + '-linux.gtk.x86_64.tar.gz" rel="nofollow" class="external-link">Linux Download</a></li><li><a class="external-link" href="https://artifactory.esss.lu.se/artifactory/CS-Studio/production/' + css_version + '/cs-studio-ess-' + css_version + '-macosx.cocoa.x86_64.zip" rel="nofollow">MacOS X Download</a></li><li><a href="https://artifactory.esss.lu.se/artifactory/CS-Studio/production/' + css_version + '/cs-studio-ess-' + css_version + '-win32.win32.x86_64.zip" rel="nofollow" class="external-link">Windows Download</a></li></ul>' \
      + production[1]

    # Construct json payload
    payload = {}
    payload["type"] = data["type"]
    payload["title"] = data["title"]
    payload["version"] = {}
    payload["version"]["number"] = data["version"]["number"]+1
    payload["body"] = {}
    payload["body"]["storage"] = {}
    payload["body"]["storage"]["value"] = result
    representation = data["body"]["view"]["representation"]
    payload["body"]["storage"]["representation"] = representation

    # Post to confluence and verify response
    put_url = base_url + page_id
    headers = {"Content-type": "application/json"}
    put_response = requests.put(put_url, data=json.dumps(payload),
                                    auth=auth, headers=headers)

    if put_response.status_code != 200:
        print("Confluence Response Code {}\nAborting"
                  .format(put_response.status_code))
        sys.exit(1)

def updateConfluenceRelease(css_version, next_version, ce_version, auth):
    """Update CSS confluence page's release notes.

    Create a new linked header and add "Compatibility Notes" and
    "Updated Features".

    Args:
        css_version: New CSS release version.
        ce_version: CSS CE version of which the CSS release is based on.
        auth: Atlassian athentication (username, password) pair.
    """
    print("\x1b[93m-- Updating Confluence release page\x1b[0m")

    base_url = "https://confluence.esss.lu.se/rest/api/content/"
    page_id = "186483068" # Use 295789998 for the dummy page

    # Pull data from the confluence page
    get_url = base_url + page_id + "?expand=body.view,version"
    get_response = requests.get(get_url, auth=auth)
    data = get_response.json()

    # Check if this version has already been put on the confluence page. If so,
    # ask user if they still want to update with the given information. If user
    # chooses 'no', skip the confluence update.
    if "Ver. " + next_version +"" in data["body"]["view"]["value"]:
        dialogMsg = "A header for CSS version {} was found on " \
                               "the confluence page. Are you sure you wish " \
                               "to update the page? [y/n]" .format(css_version)

        if not diagYes(dialogMsg):
            print("Skipping confluence update")
            return

    # Code for generating the TOC
    toc = '<p>&nbsp;</p><p><ac:structured-macro ac:name="toc" ac:schema-' \
      'version="1" ac:macro-id="a3eac23d-3226-4292-a87e-156db912bc91"/></p>'

    # Remove old toc and add new
    break_string = '"toc"> </div></p>'
    toc_stop = str(data["body"]["view"]["value"]).split(break_string)
    result = toc + toc_stop[1]

    # Update Development section
    result = re.sub(css_version, next_version, result)
    what_changed_start = result.split("What Is Changed</h4>")
    what_changed_stop = result.split("Production</h2>")
    result = what_changed_start[0] \
      + "What Is Changed</h4>" \
      + '<ul><li><span style="color: rgb(0,0,0);">Nothing yet</span>.</li></ul>' \
      +"<h2>Production</h2>" \
      + what_changed_stop[1]

    # Get url for the release
    url = 'https://jira.esss.lu.se/rest/api/2/search?jql=project=CSSTUDIO AND fixVersion="ESS CS-Studio '+css_version+'"'
    headers = {"Content-Type":"application/json"}
    response = requests.get(url, auth=auth, headers=headers)
    release_data = response.json()

    if response.status_code == 400:
        print("{} in Jira. Could not fetch changelog notes: Missing JIRA " \
                  "release?\nAborting"
                  .format(release_data["errorMessages"][0][:-1]))
        sys.exit(1)

    release_id = release_data["issues"][0]["fields"]["fixVersions"][0]["id"]
    release_url = "https://jira.esss.lu.se/projects/CSSTUDIO/versions/" + release_id

    # Update Production section
    d = datetime.date.today()
    link_address_date = d.strftime("%d.%m.%Y")
    date = d.strftime("%d.%m.%Y")
    production = result.split("Production</h2>")
    result = production[0] \
      + "Production</h2>" \
      + '<h3>Ver. ' + css_version + ' (' + date + ')</h3><ul><li><a rel="nofollow" href="' + release_url + '" class="external-link">Release Details</a></li><li><a href="https://artifactory.esss.lu.se/artifactory/CS-Studio/production/' + css_version + '/cs-studio-ess-' + css_version + '-linux.gtk.x86_64.tar.gz" rel="nofollow" class="external-link">Linux Download</a></li><li><a class="external-link" href="https://artifactory.esss.lu.se/artifactory/CS-Studio/production/' + css_version + '/cs-studio-ess-' + css_version + '-macosx.cocoa.x86_64.zip" rel="nofollow">MacOS X Download</a></li><li><a href="https://artifactory.esss.lu.se/artifactory/CS-Studio/production/' + css_version + '/cs-studio-ess-' + css_version + '-win32.win32.x86_64.zip" rel="nofollow" class="external-link">Windows Download</a></li></ul>' \
      + production[1]

    # Construct json payload
    payload = {}
    payload["type"] = data["type"]
    payload["title"] = data["title"]
    payload["version"] = {}
    payload["version"]["number"] = data["version"]["number"]+1
    payload["body"] = {}
    payload["body"]["storage"] = {}
    payload["body"]["storage"]["value"] = result
    payload["body"]["storage"]["representation"] = data["body"]["view"]["representation"]

    # Post to confluence and verify response
    put_url = base_url + page_id
    headers = {"Content-type": "application/json"}
    put_response = requests.put(put_url, data=json.dumps(payload),
                                    auth=auth, headers=headers)

    if put_response.status_code != 200:
        print("Confluence Response Code {}\nAborting"
                  .format(put_response.status_code))
        sys.exit(1)

def diagYes(string):
    """Yes/No dialog prompt.

    Args:
        string: String to be printed (yes/no question).

    Returns:
        True if user accepts.
        False if user rejects.
    """
    accepted = {"yes": True, "y": True, "no": False, "n": False}
    while True:
        choice = input(string).lower()
        if choice not in accepted:
            print("\x1b[31mPlease answer with 'y' or 'n'.\x1b[0m")
        elif not accepted[choice]:
            return False
        else:
            return True

def getCEVersion(version):
    """Get CSS CE version based on the release version.

    Args:
        string: CSS version to be released

    Returns:
        CE version deduced by removing the nano version from the CSS version.
    """
    inform("Getting CE version")

    split = version.split(".")
    return split[0] + "." + split[1] + "." + split[2]

def inform(text):
    """Inform user about what the script is currently working on.

    Args:
        text: Text to print
    """
    print("\x1b[93m-- " + text + "\x1b[0m")

def main(css_version, ignore_merge):
    """Main for automatic CSS deployment.

    Ensure user is on master branch.
    Check if release exists on Jira.
    Check if JAVA_HOME env var exists.
    Compare deploy version against artifactory and verify with user.
    Get chengelog notes by parsing all closed jira tickets for release.
    Deduce CSS CE version.
    Prepare-release (Update splash, 'about' dialog and plugin versions).
    Update changelog.
    update pom.xml.
    Merge all repositories into production.
    Update confluence Release Notes page.
    Prepare-next-release (splash, 'about' dialog and plugin, ansible reference).
    Update confluence Release page.

    Args:
        css_version: CSS version to be released.
    """
    checkBranch()                       # Ensure user is on master branch
    inform("Autenticating user")        # Output to inform user of progress
    user = input("ESS username: ")      # Used for Jira and Confluence
    passw = getpass("ESS Password: ")   # Used for Jira and Confluence
    auth = (user, passw)                # Used for Jira and Confluence
    checkJiraRelease(css_version, auth) # Check if release exists on Jira
    checkJavaHome()                     # Check if JAVA_HOME env var exists
    checkVersion(css_version)           # Check if user entered correct version

    notes = getChangelogNotes(css_version, auth) # Get changelog notes
    ce_version = getCEVersion(css_version)       # Get CSS CE version

    dir_path = os.path.dirname(os.path.abspath(__file__))+"/" # path to dir

    prepareRelease(dir_path, css_version, ce_version) # Run prepare-release.sh
    updateChangelog(dir_path, notes)                  # Update changelog
    updatePom(dir_path+"pom.xml", css_version)        # Update pom.xml

    if not ignore_merge:
        mergeRepos(dir_path+"merge.sh", css_version)  # Merge all repositories

    # Updated Confluence page with new release notes
    updateConfluenceNotes(css_version, ce_version, notes, auth)

    next_version = getNextVersion(css_version) # Get next version number
    prepareNextRelease(next_version)           # Run prepare-next-release.sh

    # Updated Confluence release page with new production and dev. versions
    updateConfluenceRelease(css_version, next_version, ce_version, auth)

    # Inform user that master branch now contains uncommited changes
    msg = "NOTE: Master branch contains uncommited changes. "
    msg += "Please revise and commit manually"
    inform(msg)

    print("\nDone")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="CSS release tool")
    parser.add_argument("css_version", type=str, help="new CSS release version")
    parser.add_argument("-i", "--ignore-merge", action="store_true",
                            help="ignore git merge")

    args = parser.parse_args()
    main(args.css_version, args.ignore_merge)
