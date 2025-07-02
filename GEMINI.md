- use ./gradlew test command to run tests
- when you implement a new function first write a comment of what it is supposed to do
- split code into small functions
- read documentation in ./docs before planning o writing code

## Writing tests

- write tests in the form given-when-then and comment each section using business logic terms. explain fully what is
  being tested and how
- do not fake tests
- do not disable tests

## Writing code.

Before writing code

## Planning

if you are asked to plan feature implementation:

1. analyze. think hard.
2. ask questions if something needs to be cleared
3. make a plan similar to this example:

Motivation: reasoning for a feature
What is planning: general overview of the change

Steps: actual steps.

After plan is accepted by user save it in the /docs/features/feature <feature name>.md file.

## Implementing a plan

If you are asked to implement a plan:

- do it for each item in the plan:
   - read plan and check if it is correct
   - ask questions if necessary
   - implement
   - ask user to check if everything is implemented and if yes mark item as complete in file
   - move to the next item

after implementing a feature update all necessary documentation in ./docs folder.

after implementing a feature update ./docs/changelod.md 


## Documentation update. 

if there is a change in plan, feature requirements or anything else always update docs in ./docs folder 
