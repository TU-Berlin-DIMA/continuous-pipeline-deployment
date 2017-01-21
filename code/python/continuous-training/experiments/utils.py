import random

import numpy as np
import pandas as pd


def split(ratings):
    testing = pd.DataFrame(columns=ratings.columns)
    counts = ratings.user.value_counts()
    test_users = counts[counts > 20].index
    for u in test_users:
        test_ratings = ratings[ratings.user == u].sample(n=5)
        testing = testing.append(test_ratings)

    rat_set = set([tuple(line) for line in ratings.values.tolist()])
    test_set = set([tuple(line) for line in testing.values.tolist()])

    training = pd.DataFrame(list(rat_set.difference(test_set)))
    return training.as_matrix(), testing.as_matrix()


def test_idx(start, end, size=5000):
    random.seed(42)
    vals = random.sample(np.arange(start, end), size)
    vals.sort()
    return vals


def load_datasets(path, sep='\t', static_data_ratio=0.1):
    names = ['user', 'item', 'rating', 'timestamp']
    df = pd.read_csv(path, sep=sep, names=names)
    n_items = df.item.unique().shape[0]
    n_users = df.user.unique().shape[0]
    df['user'] -= 1
    df['item'] -= 1
    df = df.sort_values('timestamp')
    initial_data_size = int(static_data_ratio * len(df))
    ratings = df[0:initial_data_size].as_matrix()
    stream = df[initial_data_size:].as_matrix()

    return ratings, stream, n_items, n_users
