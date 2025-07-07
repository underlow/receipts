We need to add a new feature: $ARGUMENTS. 

execute this plan step by step do not skip unless asked, stop after each step and ask confirmation:
before each step tell 'I'm executing <step name>
after each step tell '<step name> completed, next step is: <step>'

read ./docs/prd.md

## Check existing requirements
- Show me all requirements in @./docs/prd.md document which will be affected by this new feature. elaborate with me this
- Show me all requirements in @./docs/prd.md document which will conflict with this new feature. elaborate with me this until all conflicts are resolved


## Understand the requirements

- Understand the requirements described.
- understand current state of the system.


- if there are UI changes check that there is enough description to implement change, do not invent these changes.
- if there are user flow changes check that there is enough description to implement change, do not invent these changes.
- if there are changes in business entities check that there is enough description to implement change, do not invent these changes.

- Reword the change: ask yourself how you understand the change. add all details you think should be implemented and show me the result
elaborate it until the change is described and confirmed


### Validate the requirement.

repeat full Understand the requirements step until user agrees with it.  

#### Main User Scenarios & Stories section
 - check if the new feature adds a new scenario?
 - check if the new feature removes or modifies some scenarios?
 - check if the new feature conflicts with some scenarios?
 - collaborate with user and update this section.

#### Entities section
 - check if the new feature adds a new entity?
 - check if the new feature removes or modifies some entities?
 - check if the new feature conflicts with entities?
 - IMPORTANT that for every entity it's full states and it's full data fields MUST be described
 - collaborate with user and update this section.

### Functional Requirements

 - check if new items should be added
 - check if some items should be removed
 - check if some items should be changed

### UI Requirements

- check if new items should be added
- check if some items should be removed
- check if some items should be changed
  
### User Stories

- check if new items should be added
- check if some items should be removed
- check if some items should be changed

## Formulate the change
Formulate the change in a form:
Motivation - why
What we are changing
- for each UI interface should be described what exactly should be added/removed/modified

# Update the documentation. When all requirements confirmed update documentation files


## Plan
- plan is a hierarchical set of tasks where bigger tasks is split into smaller and smaller
- start thinking from top to bottom describing bigger steps and splitting them into smaller tasks.
- split tasks to smaller while it is possible
- whole plan must contain set of e2e tests which must be added.
- whole plan must contain set of user stories it covers

### Plan Items
- plan item should be as small as possible
- plan item is a atomic set of changes in app functionality
- each plan item should be implemented in tdd cycle: tests -> code -> verify
- Think of each plan item as a very small user story with its motivation and description
- creating a plan item always describe why and what problem is solves.
- each step of the plan should describe concrete set of changes. and contain user story it is implementing
- if new class is added name, behaviour, members, and it's behaviour MUST be described
- if existing class is modified each change, and it's behaviour MUST be described
- each change must contain set of tests to be added/fixed
- each change may contain set of e2e tests which must be added.
- each change must contain definition of done - what user stories are now allowed
- each plan should contain update documentation item
- instead of 'test UserController' create detailed plan like 'test user create, user delete, user update...'

# When plan is ready save it to file 
