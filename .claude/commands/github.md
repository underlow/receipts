Please analyze and implement the GitHub issue: $ARGUMENTS.

Follow these steps:

1. Use `gh issue view` to get the issue details. if this is a sub issue read parent issue to understand the context
2. Understand the problem described in the issue
3. if it is not a sub issue think if it is possible to split this issue to smaller ones which can be implemented separately. if yes ask confirmation and create sub issues and stop. Parent issue should containg items write e2e tests and update documentation. Do not close parent issue
4. if it is a parent issue and all subtickets are complete then implement it, read all subtasks comments and update documentation.if not all subtasks are implemented tell which ones and stop.
5. provide a plan to implement or fix the issue. skip this if ticked do not require code changes
6. after plan confirmed add it to ticket as checklist
7. Implement the necessary changes to implement the issue
8. Write and run tests to verify the fix. skip this if ticked do not require code changes
9. Ensure tests are passed. skip this if ticked do not require code changes
10. add to the ticket comment describing what was done and how. 
11. Create a descriptive commit message show it but do not commit without confirmation


do not add Co-Authored-By: Claude string 

Remember to use the GitHub CLI (`gh`) for all GitHub-related tasks.
