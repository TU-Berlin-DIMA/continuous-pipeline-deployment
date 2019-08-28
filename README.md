# ML Pipeline Deployment
This repository contains the source code and publications of our work on machine learning model deployment. 

## Features
- Deployment platform for machine learning pipelines
- Efficiently processes the incoming data using the deployed pipeline
- Continously update the deployed model
- Eliminates the need for periodical retraining

## Resources
- [Paper: Continuous Deployment of Machine Learning Pipelines at EDBT'19](http://openproceedings.org/2019/conf/edbt/EDBT19_paper_23.pdf)
- [Slides: EDBT'19 presentation](https://www.slideshare.net/BehrouzDerakhshan/continuous-deployment-of-machine-learning-pipelines)

## Continuous Deployment of Machine Learning Pipelines (EDBT'19)

### Abstract
Today machine learning is entering many business and scientific applications.
The life cycle of machine learning applications consists of data preprocessing for transforming the raw data into features, training a model using the features, and deploying the model for answering prediction queries.
In order to guarantee accurate predictions, one has to continuously monitor and update the deployed model and pipeline. 
Current deployment platforms update the model using online learning methods.
When online learning alone is not adequate to guarantee the prediction accuracy, some deployment platforms provide a mechanism for automatic or manual retraining of the model.
While the online training is fast, the retraining of the model is time-consuming and adds extra overhead and complexity to the process of deployment.

We propose a novel continuous deployment approach for updating the deployed model using a combination of the incoming real-time data and the historical data.
We utilize sampling techniques to include the historical data in the training process, thus eliminating the need for retraining the deployed model.
We also offer online statistics computation and dynamic materialization of the preprocessed features, which further reduces the total training and data preprocessing time.
In our experiments, we design and deploy two pipelines and models to process two real-world datasets.
The experiments show that continuous deployment reduces the total training cost up to 15 times while providing the same level of quality when compared to the state-of-the-art deployment approaches.
