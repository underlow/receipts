Please analyze and implement the GitHub issue: $ARGUMENTS.

## Ticket types

There are three types of tickets.
- root(or parent) ticket for a big feature. contains sub-tickets. All main development should be done in sub-tickets.
  Only when all sub-tickets are done root ticket can be implemented.
  In root ticket we keep only tasks which can be implemented when all sub-tickets are done. Like e2e tests, updating documentation, etc
- Sub-ticket. it is a part of a bigger feature
- Regular ticket. Not a root not a sub-ticket

## Flow for each ticket type: 

### Root
 - Use `gh issue view` to get the issue details. 
 - Understand the problem described in the issue
 - check if all sub-tickets are done. if not just report and stop
 - implement ticket if all sub-tickets are done

### Sub-ticket
 - Use `gh issue view` to get the issue details. read parent issue to understand the context
 - Understand the problem described in the issue
 - plan development
 - write tests first, check that tests fail
 - implement feature
 - Ensure tests are passed
 - add to the ticket comment describing what was done and how
 - Create a descriptive commit message show it but do not commit and do not stage files to git 

## Regular ticket
 - Use `gh issue view` to get the issue details.
 - Understand the problem described in the issue
 - if it is not a sub issue think if it is possible to split this issue to smaller ones which can be implemented separately.
 - if it is possible to split it then create sub issues and stop. leave items write e2e tests and update documentation in root ticket. stop after creating tickets  
 - plan development
 - write tests first, be sure that new tests a failing
 - implement feature
 - Ensure tests are passed
 - add to the ticket comment describing what was done and how
 - Create a descriptive commit message show it but do not commit and do not stage files to git


NEVER consider the task complete if even one test is failing

Remember to use the GitHub CLI (`gh`) for all GitHub-related tasks.
