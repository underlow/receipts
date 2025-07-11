- when you implement a new function first write a comment of what it is supposed to do
- split code into small readable functions
- follow industry best practices

- each important entity MUST be documented in /docs/prd.md Before any development entity states and data MUST be fully defined and documented.
- any interface MUST be documented. each item of the interface MUST be documented. behaviour of every interface item must be documented and tested with unit and e2e tests. 

- NEVER create copy of functionality in test classes

kotlin code should never generate html. 

logs and console messages from tests do not appear in test logs. do not use them.

# Bash commands

- ./gradlew test: Run unit tests

## Documentation

All documentation are in the ./docs folder:
./docs/architecture.md - overall architecture

all docs MUST be updated after each new feature implementation.

## Github

all changes should have corresponding github issue. if you are asked to make a code change first create an issue using `gh`
before implementing a feature or fixing a bug add create plan into github issue as a checklist
after implementing add to ticket information what was implemented and how.  

## Writing tests

- write tests in the form given-when-then and comment each section using business logic terms. explain fully what is
  being tested and how
- do not fake tests
- do not disable tests
- do not write trivial tests. for example do not test constructors

### e2e tests

 - Give each test a single responsibility and a descriptive name (e.g. shouldShowErrorWhenPasswordTooShort)
 - Use Page Objects or modules to wrap selectors and actions—tests should read like a story, not low-level clicks.
 - Rely on data-test-id or similarly dedicated attributes instead of brittle CSS/XPath paths.
 - Each test must leave the system in a clean state; avoid sharing state or ordering dependencies.
 - Chain readable assertions (e.g. element.shouldHave(text(“…”)).shouldBe(visible)) with clear failure messages
 - Favor clarity over cleverness: explicit steps, small helper names, and sparing comments for non-obvious logic.
 - helpers are here package me.underlow.receipt.e2e.helpers
 - pages objects are here package me.underlow.receipt.e2e.pages
 - e2e test must test full code flow from frontend layer to db layes, do not implement shortcuts in the middle. bad: add js code raising error on frontend. good: initiate actions which lead to error
 - if you are changing data-test-id check if it uses in other tests

### fixing failing test. 
- first you have to check code to explore if the problem in test or in functionality. 
- if the testing feature doesn't work - fix the feature not the test. 

## Planning

A proper plan should follow the TDD approach: implement → test → verify → commit for each small change.
Split all work to small tasks which can be implemented, tested and commited without breaking code.

if you are asked to plan feature implementation:

- read all docs and find out which files related to this new feature
- ask questions if something needs to be cleared
- analyze requirements. think hard.
- be concrete, describe all changes at class and functions level.
- don't leave anything to be explored later, at implementation stage everything should already be known and documented.

## Implementing a plan

If you are asked to implement a plan:

- do it for each item in the plan:
    - read plan and check if it is correct
    - ask questions if necessary
    - implement
    - ask user to check if everything is implemented and if yes mark item as complete in file
    - move to the next item


## Testing

- write tests before implementing function.
- confirm tests are failing
- write code
- confirm tests are passed
- do not test trivial things, class creation etc

# Code style

- use kotlin official code style
- use immutable data structures when possible 
- write comments to data structures and functions
- keep files small. split them when possible
  - for html extract css and fragments 
  - for tests separate tests into several classes grouped by logic

to explore a codebase read ./docs/code.md
update ./docs/code.md after each change in code structure
