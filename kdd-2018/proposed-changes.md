# Propose Changes for KDD 2018 Submission

- We should modify the text and put more emphasis on the 'deployment' and 'system' aspect rather than the 'training' part. VLDB 2017 reviewers were mostly concern with the correctness of the machine learning theory and seemed to like what the paper was addressing, a deployment system for ML models. After fixing the issues raised by the VLDB 2017 reviewers, we have changed the focus to how we train a model rather than how we deploy it. Understandably, SIGMOD reviewers raised the issue that the content is not very original. 
- We should change to prequential testing (or at least include that as another parameter for testing the accuracy)
- Increase the duration of the simulation. Currently, we are only using two days of data for training and one day for testing. 
- We should use stronger baselines for our experiments (FTLR) as mentioned by the SIGMOD Reviewers
- We should address how we can alleviate surges and bursts in the queries.
- We should perform another set of experiments with another dataset and ML model (regression, recommender, NN, ...) 
- Address how we need to address we can forget the past and quickly start with a new model

I plan to start by going through the paper once and mark part of the texts that were directly criticised by the reviewers. 
Once we address those, we should decrease the length of the document to 9. 
Shift the focus to a deployment system. 
Once these are done, we can focus on fixing the experiments.
