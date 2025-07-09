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

to explore a codebase read ./docs/code.md
update ./docs/code.md after each change in code structure


# Using Gemini CLI for Analysis

When analyzing codebase or multiple files, use the Gemini CLI. 
Use `gemini -p` to leverage Google Gemini's large context capacity.

## File and Directory Inclusion Syntax

Use the `@` syntax to include files and directories in your Gemini prompts. The paths should be relative to WHERE you run the
gemini command:

### Examples:

**Single file analysis:**
gemini -p "@src/main.py Explain this file's purpose and structure"

Multiple files:
gemini -p "@package.json @src/index.js Analyze the dependencies used in the code"

Entire directory:
gemini -p "@src/ Summarize the architecture of this codebase"

Multiple directories:
gemini -p "@src/ @tests/ Analyze test coverage for the source code"

Current directory and subdirectories:
gemini -p "@./ Give me an overview of this entire project"

# Or use --all_files flag:
gemini --all_files -p "Analyze the project structure and dependencies"

Implementation Verification Examples

Check if a feature is implemented:
gemini -p "@src/ @lib/ Has dark mode been implemented in this codebase? Show me the relevant files and functions"

Search the code:
gemini --all_files -p "Show me all usages of InboxController OCR endpoints"

Check for specific patterns:
gemini -p "@src/ Are there any React hooks that handle WebSocket connections? List them with file paths"

Verify error handling:
gemini -p "@src/ @api/ Is proper error handling implemented for all API endpoints? Show examples of try-catch blocks"

Verify test coverage for features:
gemini -p "@src/payment/ @tests/ Is the payment processing module fully tested? List all test cases"

When to Use Gemini CLI

Use gemini -p when:
- Analyzing entire codebases or large directories
- Comparing multiple large files
- Need to understand project-wide patterns or architecture
- Current context window is insufficient for the task
- Working with files totaling more than 100KB
- Verifying if specific features, patterns, or security measures are implemented
- Checking for the presence of certain coding patterns across the entire codebase

Important Notes

- Paths in @ syntax are relative to your current working directory when invoking gemini
- The CLI will include file contents directly in the context
- No need for --yolo flag for read-only analysis
- Gemini's context window can handle entire codebases that would overflow Claude's context
- When checking implementations, be specific about what you're looking for to get accurate results
