from __future__ import print_function
from __future__ import division

import time

import numpy as np

from experiments.utils import load_datasets, test_idx
from matrix_factorization.matrix_factorization import MatrixFactorizationModel
from optimizer.stochasticgradientdescent import StochasticGradientDescent

REPORT_INCREMENT = 1000


def full():
    start_time = time.time()
    # ratings, stream, n_items, n_users = load_datasets('datasets/ml-1m/ratings.dat', sep='::', static_data_ratio=1.0)
    # n_items = 3952
    ratings, stream, n_items, n_users = load_datasets('datasets/ml-100k/u.data', static_data_ratio=1.0)
    test = [i + 100000 for i in test_idx(0, 900000)]
    tests = ratings[test, :]
    ratings = np.delete(ratings, test, 1)
    model = MatrixFactorizationModel(item_count=n_items, user_count=n_users, sum_rating=np.sum(ratings[:, 2]),
                                     count_rating=len(ratings[:, 2]))
    sgd = StochasticGradientDescent(model, ratings[:, (0, 1)], ratings[:, 2])
    sgd.train(num_iterations=100)
    model.calculate_loss(tests[:, 0:2], tests[:, 2])
    running_time = time.time() - start_time
    return model.calculate_loss(tests[:, 0:2], tests[:, 2]), running_time


# start with a batch then continue with online learning
def offline_online():
    start_time = time.time()
    ratings, stream, n_items, n_users = load_datasets('datasets/ml-100k/u.data', static_data_ratio=0.1)
    # ratings, stream, n_items, n_users = load_datasets('datasets/ml-1m/ratings.dat', sep='::', static_data_ratio=0.1)
    # n_items = 3952
    model = MatrixFactorizationModel(item_count=n_items, user_count=n_users, sum_rating=np.sum(ratings[:, 2]),
                                     count_rating=len(ratings[:, 2]))
    sgd = StochasticGradientDescent(model, ratings[:, (0, 1)], ratings[:, 2])
    sgd.train()
    test = test_idx(0, 900000)
    error = []
    i = 0
    idx = 0
    test_set = np.empty([0, stream.shape[1]], dtype=np.uint64)
    for item in stream:
        if i == test[idx]:
            test_set = np.vstack([test_set, item])
            error_rate = model.calculate_loss(test_set[:, 0:2], test_set[:, 2])
            idx += 1
            error += [error_rate]
            if idx == len(test):
                idx = 0
            print("{} items process".format(i))
        else:
            sgd.incremental(item[0:2], item[2], 0.001)
        i += 1

    error_rate = model.calculate_loss(test_set[:, 0:2], test_set[:, 2])
    error += [error_rate]

    running_time = time.time() - start_time
    return error, running_time


# only includes initial training
def offline_only():
    start_time = time.time()
    #ratings, stream, n_items, n_users = load_datasets('datasets/ml-100k/u.data', static_data_ratio=0.1)
    ratings, stream, n_items, n_users = load_datasets('datasets/ml-1m/ratings.dat', sep='::', static_data_ratio=0.1)
    n_items = 3952
    model = MatrixFactorizationModel(item_count=n_items, user_count=n_users, sum_rating=np.sum(ratings[:, 2]),
                                     count_rating=len(ratings[:, 2]))
    sgd = StochasticGradientDescent(model, ratings[:, (0, 1)], ratings[:, 2])
    sgd.train()
    test = test_idx(0, 900000)
    error = []
    i = 0
    idx = 0
    test_set = np.empty([0, stream.shape[1]], dtype=np.uint64)
    for item in stream:
        if i == test[idx]:
            test_set = np.vstack([test_set, item])
            error_rate = model.calculate_loss(test_set[:, 0:2], test_set[:, 2])
            idx += 1
            error += [error_rate]
            if idx == len(test):
                idx = 0
            print("{} items process".format(i))
        i += 1

    error_rate = model.calculate_loss(test_set[:, 0:2], test_set[:, 2])
    error += [error_rate]

    running_time = time.time() - start_time
    return error, running_time


# online learning from the very beginning
def naive():
    start_time = time.time()
    # ratings, stream, n_items, n_users = load_datasets('datasets/ml-1m/ratings.dat', sep='::', static_data_ratio=0.0)
    # n_items = 3952
    ratings, stream, n_items, n_users = load_datasets('datasets/ml-100k/u.data', static_data_ratio=0.0)
    model = MatrixFactorizationModel(item_count=n_items, user_count=n_users)
    sgd = StochasticGradientDescent(model, np.empty([0, stream.shape[1]], dtype=np.uint64),
                                    np.empty([0, 1], dtype=np.uint64))
    error = []
    test = [i + 100000 for i in test_idx(0, 900000)]
    idx = 0
    i = 0
    test_set = np.empty([0, stream.shape[1]], dtype=np.uint64)
    for item in stream:
        if i == test[idx]:
            test_set = np.vstack([test_set, item])
            error_rate = model.calculate_loss(test_set[:, 0:2], test_set[:, 2])
            idx += 1
            error += [error_rate]
            if idx == len(test):
                idx = 0
            print("{} items process".format(i))
        else:
            sgd.incremental(item[0:2], item[2], 0.001)
        i += 1

    error_rate = model.calculate_loss(test_set[:, 0:2], test_set[:, 2])
    error += [error_rate]

    running_time = time.time() - start_time
    return error, running_time


# continuous batch and online learning
def continuous(sample_rate=1.0, buffer_size=500):
    start_time = time.time()
    # ratings, stream, n_items, n_users = load_datasets('datasets/ml-1m/ratings.dat', sep='::', static_data_ratio=0.1)
    # n_items = 3952
    ratings, stream, n_items, n_users = load_datasets('datasets/ml-100k/u.data', static_data_ratio=0.1)
    model = MatrixFactorizationModel(item_count=n_items, user_count=n_users, sum_rating=np.sum(ratings[:, 2]),
                                     count_rating=len(ratings[:, 2]))
    sgd = StochasticGradientDescent(model, ratings[:, (0, 1)], ratings[:, 2])
    sgd.train()
    error = []
    i = 0
    test = test_idx(0, 90000)
    idx = 0
    test_set = np.empty([0, stream.shape[1]], dtype=np.uint64)
    for item in stream:
        if i == test[idx]:
            test_set = np.vstack([test_set, item])
            error_rate = model.calculate_loss(test_set[:, 0:2], test_set[:, 2])
            idx += 1
            error += [error_rate]
            if idx == len(test):
                idx = 0
        else:
            sgd.incremental(item[0:2], item[2], 0.001)
            sgd.update_buffer(item[0:2], item[2])
        if (i % buffer_size == 0) & (i != 0):
            print("{} items process ... scheduling new iteration".format(i))
            sgd.run_iter(sample_rate=sample_rate)
        i += 1

    if len(sgd.buffer_y) > 0:
        sgd.run_iter()

    error_rate = model.calculate_loss(test_set[:, 0:2], test_set[:, 2])
    error += [error_rate]

    running_time = time.time() - start_time
    return error, running_time


# velox method, start with batch, continue with online when error rate goes below a threshold retrain from scratch
def velox():
    start_time = time.time()
    # ratings, stream, n_items, n_users = load_datasets('datasets/ml-1m/ratings.dat', sep='::', static_data_ratio=0.1)
    # n_items = 3952
    ratings, stream, n_items, n_users = load_datasets('datasets/ml-100k/u.data', static_data_ratio=0.1)
    model = MatrixFactorizationModel(item_count=n_items, user_count=n_users, sum_rating=np.sum(ratings[:, 2]),
                                     count_rating=len(ratings[:, 2]))
    sgd = StochasticGradientDescent(model, ratings[:, (0, 1)], ratings[:, 2])
    sgd.train()
    error = []
    i = 0
    buffer_size = 150000
    test = test_idx(0, 900000)
    idx = 0
    test_set = np.empty([0, stream.shape[1]], dtype=np.uint64)
    for item in stream:
        if i == test[idx]:
            test_set = np.vstack([test_set, item])
            error_rate = model.calculate_loss(test_set[:, 0:2], test_set[:, 2])
            idx += 1
            error += [error_rate]
            if idx == len(test):
                idx = 0
        else:
            sgd.incremental(item[0:2], item[2], 0.001)
            sgd.update_buffer(item[0:2], item[2])
        if (i % buffer_size == 0) & (i != 0):
            print("{} items process ... scheduling new iteration".format(i))
            sgd.retrain()
        i += 1

    if len(sgd.buffer_y) > 0:
        sgd.retrain()

    error_rate = model.calculate_loss(test_set[:, 0:2], test_set[:, 2])
    error += [error_rate]

    running_time = time.time() - start_time
    return error, running_time


# error_full, time_full = full()
# error_off_on, time_off_on = offline_online()
# error_continuous, time_continuous = continuous()
# error_velox, time_velox = velox()
# error_naive, time_naive = naive()
# error_off, time_off = offline_only()

error_buffers = []
times_buffers = []
for s in np.arange(500, 5500, 500):
    print("Buffer Size {}".format(s))
    error_continuous, time_continuous = continuous(buffer_size=s)
    error_buffers += [error_continuous]
    times_buffers += [time_continuous]
