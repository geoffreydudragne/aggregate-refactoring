# Refactor to aggregate kata

This repo provides a pseudo application based on use cases of meetup subscription. It integrates some rules about 
participants and waiting list. The main class is covered with integration tests that start an h2 memory database, 
so the persistence part can safely be 
changed.

The current implementation delegates a lot of its behavior to the DAO methods, which results in a pretty strong coupling 
between the code and the SQL. 

The goal of this exercise is to refactor the code to design it with the aggregate pattern 
from DDD. It should allow to better understand how the pattern works in practice, and what are the advantages or difficulties.
The fully refactored code should present less coupling between the persistence and the domain behavior.

## Aggregate pattern

Short explanation of the pattern: https://martinfowler.com/bliki/DDD_Aggregate.html

## Suggested steps

To transform the current code, here are the suggested macro steps

1. Implement and test the  `findById` and `save` methods for the MeetupEventRepository, allowing to find or save as a 
whole a meetup event with all the associated subscriptions

2. Reimplement the DAO methods with plain java and `findById` and`findById` methods from the MeetupEventRepository

3. Inline the DAO methods into the main app class

4. Rearrange every use case code so that `findById` is the first statement, and `save` is the last statement, leaving in the 
middle only plain java behavior code

5. Extract the behavior code to the MeetupEvent domain class and clean the code

## Help

Because writing the sql can be annoying (and vendor specific to the database), the tricky elements are already available 
as private methods in the MeetupEventRepository.

A branch with the full implementation of the Repository and test is also provided: `with-repo-implem`