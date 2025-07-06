We need to add a new feature: $ARGUMENTS.

## Ticket types

There are three types of tickets.
- root(or parent) ticket for a big feature. contains sub-tickets. All main development should be done in sub-tickets.
  Only when all sub-tickets are done root ticket can be implemented. 
  In root ticket we keep only tasks which can be implemented when all sub-tickets are done. Like e2e tests, updating documentation, etc 
  creating this ticket is important to outline what part of the plan will be implemented in sub-tickets and what part should be implemented in this ticket
- Sub-ticket. it is a part of a bigger feature
- Regular ticket. Not a root not a sub-ticket

## Plan 

 - each step of the plan should describe concrete set of changes. 
 - for each class should be described which class members should be changed and how they should be changed
 - for each UI interface should be described what exactly should be added/removed/modified
 - each change must contain set of tests to be added/fixed
 - each change must contain set of e2e tests which must be added. 
 - each change must contain definition of done - what user stories are now allowed
 - each plan should contain update documentation item

## Tickets

each ticked must have:
 - motivation or problem description
 - definition of done
 - detailed implementation plan

Follow these steps:

- Understand the requirements described 
- Analyse how it will fit into current requirements 
- challenge requirements. ask questions if needed. 
- Show me all requirements in @./docs/prd.md document which will be affected by this new feature. elaborate with me this
- Show me all requirements in @./docs/prd.md document which will conflict with this new feature. elaborate with me this until all conflicts are resolved 
- think if it is possible to split this issue to smaller ones which can be implemented separately. 
- provide a plan to implement feature, plan should be concrete and should be made of smallest possible steps. each step should be one change that can be delivered 
- tend to use vertical split of functionality, 
  plan of several items like 'implement one method in repository, controller and ui' 
  is better than 'implement full dao layer', 'implement full controlles layer' ... 
- after plan confirmed create github tickets. when creating tickets you have to decide if we need set of separate tickets or one umbrella tickets with sub-tickets  
- after plan confirmed ./docs/prd.md ./docs/architecture.md must be updated.

Remember to use the GitHub CLI (`gh`) for all GitHub-related tasks.
