- when you implement a new function first write a comment of what it is supposed to do
- split code into small functions

# Bash commands

- ./gradlew test: Run unit tests

## Documentation

All documentation are in the ./docs folder:
./docs/architecture.md - overall architecture
./docs/overview.md - functionality overview

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

## Planning

if you are asked to plan feature implementation:

- read all docs and find out which files related to this new feature
- ask questions if something needs to be cleared
- analyze requirements. think hard.
- be concrete, describe all changes at class and functions level.
- don't leave anything to be explored later, at implementation stage everything should already be known and documented.
  worst case scenario - first item in the plan can be research item
- make a plan similar to this example:

Motivation: reasoning for a feature
Feature: describe feature in detail. update this on each change

Definition of done: describe what tests should be written, describe all test cases to check

Steps: actual steps.

Save plan in the /docs/features/<github issue id or other prefix - ask for it> <feature name>.md file before
implementing.
Update file on each plan change

## Implementing a plan

If you are asked to implement a plan:

- do it for each item in the plan:
    - read plan and check if it is correct
    - ask questions if necessary
    - implement
    - ask user to check if everything is implemented and if yes mark item as complete in file
    - move to the next item

# Workflow

- Be sure to run tests when you’re done making a series of code changes

## Testing

- write tests before implementing function.
- confirm tests are failing
- write code
- confirm tests are passed
- do not test trivial things, class creation etc

# Code style

- use kotlin official code style
- use immutable data structures when possible 
