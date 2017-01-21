from __future__ import division

import math
import random
import time

import numpy as np
import pandas as pd
import scipy.io as sio
from sklearn.neural_network import MLPClassifier


def creditcard():
    df = pd.read_csv('datasets/credit-card/creditcard.csv')
    features = [col for col in df.columns if col not in ['Class']]
    target = 'Class'

