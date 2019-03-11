#!/usr/bin/env python3
#
#  Copyright (c) 2019 Johannes C. Kazantzidis
#
#  The program is free software: You can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published
#  by the Free Software Foundation.
#
#  This program is provided as is, WITHOUT ANY WARRANTY; without even
#  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
#  PURPOSE.  See the GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License along with
#  this program. If not, see https://www.gnu.org/licenses/gpl.txt

__version__ = '0.0.1'
__author__ = 'Johannes C. Kazantzidis'

import argparse
import datetime
import distutils
import fileinput
from getpass import getpass
import os
import platform
import re
import subprocess
import sys

import requests
from requests.auth import HTTPBasicAuth
import json

def checkJavaHome():
    """Check if user has `JAVA8_HOME` environment variable set.

    The variable needed for the `prepare-release.sh` and
    `prepare-next-release.sh` scripts
    """
    if "JAVA8_HOME" in os.environ:
        return # env var found, all good
    elif platform.system() == "Linux":
        print("You don't seem to have a path in the JAVA8_HOME variable")
        suggestion_cmd = "dirname $(readlink -f $(which java))"
    elif platform.system() == "Darwin":
        suggestion_cmd = "dirname $(readlink $(which java))"

    suggestion = subprocess.check_output(suggestion_cmd,
                                             shell=True).decode("utf8")

    print("Put the following your `.profile` file or `.bashrc` file:\n{}"
                  .format(suggestion))

    # Ask if user wants to try to continue even though `JAVA8_HOME` was not found
    accepted = {"yes": True, "y": True, "no": False, "n": False}
    while True:
        choice = input("Would you still like to proceed? [y/n]").lower()
        if choice not in accepted:
            print("Please answer with 'y' or 'n'.")
        elif accepted[choice]:
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
        accepted = {"yes": True, "y": True, "no": False, "n": False}
        while True:
            choice = input("Are you sure you wish to use {}? [y/n]"
                                   . format(user_input)).lower()
            if choice not in accepted:
                print("\x1b[31mPlease answer with 'y' or 'n'.\x1b[0m")
            elif not accepted[choice]:
                print("Aborting")
                sys.exit()
            else:
                return

def prepareRelease(path, release_url, version, notes, ce_version):
    """Run `prepare-release.sh`.

    `prepare-release.sh` is a community developed script for creating
    new splash screen, change 'about' dialog, change Ansible reference
    file, update plugin versions, update product versions in product
    files, update product versions in master POM file and
    commit-tag-push changes.

    Args:
        path: Path to `prepare-release.sh`.
        release_url: Url to Jira release page.
        version: CSS release version to prepare.
        notes: Notes to be inserted into the changelog.
        ce_version: CSS CE version of which the CSS release is based on.
    """
    prepare_release_cmd = str(
        'bash {}prepare-release.sh {} "{}" "{}</li><li>' \
        'Based on CS-Studio CE {}-SNAPSHOT" true'
    .format(path, version, release_url, notes, ce_version))

    try:
        subprocess.check_call(prepare_release_cmd, shell=True)
    except subprocess.CalledProcessError as e:
        print("")
        print("Something went wrong when running the 'prepare-release.sh' " \
                  "script. Check the line above this, it is likely that the " \
                  "git tag (ESS-CSS-{}) already exists. If you still " \
                  "want to run this deployment, type " \
                  "'git tag --delete ESS-CSS-{}' and re-run this script"
                  .format(version, version))
        print("\nAborting")
        #TODO: automatic delete of tag if user wants to?
        sys.exit()

def prepareNextRelease(version): #TODO: Test function
    """Run `prepare-next-release.sh`.

    `prepare-next-release.sh` is a community developed script for
    preparing splash screen, change 'about' dialog, change Ansible
    reference file, updating plugin versions, update product versions in
    master POM file, commit version and push changes.

    Args:
        version: CSS version of current release, i.e. not next version.
    """
    # Determine next version by incrementing nano version of the current release
    next_version = ".".join([
        ".".join(version.split(".")[:-1]),
        str(int(version.split(".")[-1])+1)
        ])

    # Verify version with user
    print("Suggested version number for next release is {}"
              .format(next_version))

    accepted = {"yes": True, "y": True, "no": False, "n": False}
    while True:
        choice = input("Is this correct{}? [y/n]"
                             . format(user_input)).lower()
        if choice not in accepted:
            print("\x1b[31mPlease answer with 'y' or 'n'.\x1b[0m")
        elif not accepted[choice]:
            next_version = input("Enter next version number?")
            break
        else:
            break

    path = os.path.dirname(os.path.abspath(__file__))+"/"

    prepare_next_release_cmd = str("bash {} prepare-next-release.sh {} false"
                                       .format(path, next_version))

    subprocess.check_call(prepare_next_release_cmd, shell=True)

def replace(path, pattern, repl):
    """Replace a pattern in a file.

    Inplace relpacement of text in a file. Original file will be backed
    up with <filename>.bak

    Args:
        path: Path to file in which to replace text.
        pattern: Regular expression pattern to search fore.
        repl: Replacement text with which to replace text matching to `pattern`.
    """
    pat = re.compile(pattern)
    with fileinput.FileInput(path, inplace=True, backup=".bak") as file:
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
    split = version.split(".")
    majmin = ".".join(split[0:2])
    pattern = "<cs-studio.version>[0-9]+\.[0-9]+</cs-studio.version>"
    replacement = "<cs-studio.version>" + majmin + "</cs-studio.version>"
    replace(path, pattern, replacement)

def getChangelogNotes(version, auth):
    """Get notes for changelog from Jira.

    Get notes from Jira via REST interface and format the notes to be
    accepted by the `prepare-release.sh` script (see function
    `prepareRelease` in this file for more info.).

    Args:
        version: Full CSS version number to be released, e.g. 4.6.1.12
    """
    # REST url for issues specific for the release
    url = 'https://jira.esss.lu.se/rest/api/2/search?jql=project=CSSTUDIO AND fixVersion="ESS CS-Studio '+version+'"'

    headers = {"Content-Type":"application/json"}
    response = requests.get(url, auth=auth, headers=headers)
    data = response.json()

    if response.status_code == 400:
        print("{} in Jira.\nCould not fetch changelog notes: missing JIRA release?\nAborting"
                  .format(data["errorMessages"][0][:-1]))
        sys.exit(1)

    note_list = []
    pattern = re.compile("CSS-CE #[0-9]+")

    # `CSS-CE #XXX` is a merge from the community version. Sort `note_list`
    # with CSS-CE merges first.
    for issue in data["issues"]:
        summary = issue["fields"]["summary"]
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

def mergeRepos(path, version):
    """Merge all relevant repositories into production.

    Args:
        path: Path to `merge.sh`.
        version: CSS version for this release.
    """
    merge_cmd = str("bash {} {}" .format(path, version))

    subprocess.check_call(merge_cmd, shell=True)

def updateConfluence(css_version, ce_version, notes, auth):
    """Update CSS confluence page's release notes.

    Create a new linked header and add "Compatibility Notes" and
    "Updated Features".

    Args:
        css_version: New CSS release version.
        ce_version: CSS CE version of which the CSS release is based on.
        notes: Notes to be inserted into the changelog.
        auth: Atlassian athentication (username, password) pair.
    """
    base_url = "https://confluence.esss.lu.se/rest/api/content/"
    page_id = "295789998" # Use 129630590 for the real page

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
    new_section = '<h2><a href=' + header_link + ' rel="nofollow">Ver.' \
      + css_version + '</a>    - ' + header_date + \
      '</h2><h3>Compatibility Notes</h3>' \
      '<ul><li>Based on the most recent source code of CS-Studio CE&nbsp;' \
      + ce_version + '-SNAPSHOT.</li></ul><h3>Updated Features</h3><ul><li>' \
      + notes + '</li></ul>'

    # Check if this version has already been put on the confluence page. If so,
    # ask user if they still want to update with the given information. If user
    # chooses 'no', skip the confluence update.
    if "Ver. " + css_version +"" in data["body"]["view"]["value"]:
        accepted = {"yes": True, "y": True, "no": False, "n": False}
        while True:
            choice = input("A header for CSS version {} was found on " \
                               "the confluence page. Are you sure you wish " \
                               "to post your notes? [y/n]"
                               .format(css_version)).lower()

            if choice not in accepted:
                print("\x1b[31mPlease answer with 'y' or 'n'.\x1b[0m")
            elif not accepted[choice]:
                print("Skipping confluence update")
                return
            else:
                break

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

    payload["body"]["storage"]["representation"] = data["body"]["view"]["representation"]

    # Post to confluence and verify response
    put_response = requests.put(put_url, data=json.dumps(payload),
                                    auth=auth, headers=headers)

    if put_response.status_code != 200:
        print("Response code {}\nAborting" .format(put_response.status_code))
        sys.exit(1)

def main(css_version, ce_version):
    """Main for automatic CSS deployment.

    This script performs the following steps:

    1. Check if user has `JAVA8_HOME` environment variable set:
    The variable needed for the `prepare-release.sh` and
    `prepare-next-release.sh` scripts

    2. Checks the CSS version input against artifactory:
    Grabs latest CSS version number from
    artifactory.esss.lu.se/artifactory/CS-Studio/production/
    and increments nano version (i.e. last number) by one. If the
    resulting number differs from user input, the user is prompted for
    verification to continue.

    3. Get notes for changelog from Jira:
    Get notes from Jira via REST interface and format the notes to be
    accepted by the `prepare-release.sh` script.

    4. Run `prepare-release.sh`:
    `prepare-release.sh` is a community developed script for creating
    new splash screen, change 'about' dialog, change Ansible reference
    file, update plugin versions, update product versions in product
    files, update product versions in master POM file and
    commit-tag-push changes.

    5. Update pom.xml file:
    Update cs-studio major, and minor, version number in pom.xml file.

    6. Merge all relevant repositories into production.

    7. Update CSS confluence page's release notes:
    Create a new linked header and add "Compatibility Notes" and
    "Updated Features".

    Args:
        css_version: New CSS release version.
        ce_version: CS-Studio CE version that new release is based on.
    """
    checkJavaHome()
    checkVersion(css_version)
    release_url = "https://jira.esss.lu.se/projects/CSSTUDIO/versions/23001"
    dir_path = os.path.dirname(os.path.abspath(__file__))+"/"

    user = input("ESS username: ")    # Used for Jira and Confluence
    passw = getpass("ESS Password: ") # Used for Jira and Confluence
    auth = (user, passw)              # Used for Jira and Confluence
    notes = getChangelogNotes(css_version, auth)
    prepareRelease(dir_path, release_url, css_version, notes, ce_version)
    updatePom(dir_path+"pom.xml", css_version)
    mergeRepos(dir_path+"merge.sh", css_version)
    updateConfluence(css_version, ce_version, notes, auth)

    print("\nDone")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="CSS release tool")
    parser.add_argument("css_version", type=str, help="New CSS release version")
    parser.add_argument("ce_version", type=str, help="CS-Studio CE version " \
                            "that new release is based on")

    args = parser.parse_args()

    main(args.css_version, args.ce_version)
