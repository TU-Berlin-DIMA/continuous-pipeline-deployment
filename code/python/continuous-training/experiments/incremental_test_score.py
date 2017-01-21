from __future__ import division

import time

import matplotlib.pyplot as plt
import numpy as np

from experiments.utils import load_datasets
from matrix_factorization.matrix_factorization import MatrixFactorizationModel
from optimizer.stochasticgradientdescent import StochasticGradientDescent

REPORT_INCREMENT = 1000


def full():
    start_time = time.time()
    # ratings, stream, n_items, n_users = load_datasets('datasets/ml-100k/u.data', static_data_ratio=0.1)
    ratings, stream, n_items, n_users = load_datasets('datasets/ml-1m/ratings.dat', sep = '::', static_data_ratio=1.0)
    n_items = 3952
    model = MatrixFactorizationModel(item_count=n_items, user_count=n_users, sum_rating=np.sum(ratings[:, 2]),
                                     count_rating=len(ratings[:, 2]))
    sgd = StochasticGradientDescent(model, ratings[:, (0, 1)], ratings[:, 2])
    sgd.train()
    error = model.calculate_loss(ratings[:, (0, 1)], ratings[:, 2])
    running_time = time.time() - start_time
    return error, running_time


# start with a batch then continue with online learning
def offline_online():
    start_time = time.time()
    # ratings, stream, n_items, n_users = load_datasets('datasets/ml-100k/u.data', static_data_ratio=0.1)
    ratings, stream, n_items, n_users = load_datasets('datasets/ml-1m/ratings.dat', sep = '::', static_data_ratio=0.1)
    n_items = 3952
    model = MatrixFactorizationModel(item_count=n_items, user_count=n_users, sum_rating=np.sum(ratings[:, 2]),
                                     count_rating=len(ratings[:, 2]))
    sgd = StochasticGradientDescent(model, ratings[:, (0, 1)], ratings[:, 2])
    sgd.train()
    error = []
    i = 0
    cum_error = 0
    for item in stream:
        l = np.abs(model.loss(item[0:2], item[2]))
        cum_error += l
        sgd.incremental(item[0:2], item[2], 0.001)
        i += 1
        if i % 1000 == 0:
            print "{} items processed".format(i)
            error += [cum_error / i]
    running_time = time.time() - start_time
    return error, running_time


# online learning from the very beginning
def naive():
    start_time = time.time()
    # ratings, stream, n_items, n_users = load_datasets('datasets/ml-100k/u.data', static_data_ratio=0.1)
    ratings, stream, n_items, n_users = load_datasets('datasets/ml-1m/ratings.dat', sep = '::', static_data_ratio=0.0)
    n_items = 3952
    model = MatrixFactorizationModel(item_count=n_items, user_count=n_users)
    sgd = StochasticGradientDescent(model, np.empty([0, stream.shape[1]], dtype=np.uint64),
                                    np.empty([0, 1], dtype=np.uint64))
    error = []
    i = 0
    cum_error = 0
    for item in stream:
        l = np.abs(model.loss(item[0:2], item[2]))
        cum_error += l
        sgd.incremental(item[0:2], item[2], 0.001)
        i += 1
        if i % 1000 == 0:
            error += [cum_error / i]

    running_time = time.time() - start_time
    return error, running_time


# continuous batch and online learning
def continuous():
    start_time = time.time()
    # ratings, stream, n_items, n_users = load_datasets('datasets/ml-100k/u.data', static_data_ratio=0.1)
    ratings, stream, n_items, n_users = load_datasets('datasets/ml-1m/ratings.dat', sep = '::', static_data_ratio=0.1)
    n_items = 3952
    model = MatrixFactorizationModel(item_count=n_items, user_count=n_users, sum_rating=np.sum(ratings[:, 2]),
                                     count_rating=len(ratings[:, 2]))
    sgd = StochasticGradientDescent(model, ratings[:, (0, 1)], ratings[:, 2])
    sgd.train()
    error = []
    i = 0
    cum_error = 0
    buffer_size = 50000
    for item in stream:

        l = np.abs(model.loss(item[0:2], item[2]))
        cum_error += l
        sgd.incremental(item[0:2], item[2], 0.001)
        sgd.update_buffer(item[0:2], item[2])
        if (i % buffer_size == 0) & (i != 0):
            print "{} items process ... scheduling new iteration".format(i)
            sgd.run_iter()
        i += 1
        if i % 1000 == 0:
            error += [cum_error / i]
    running_time = time.time() - start_time
    return error, running_time


# velox method, start with batch, continue with online when error rate goes below a threshold retrain from scratch
def velox():
    start_time = time.time()
    # ratings, stream, n_items, n_users = load_datasets('datasets/ml-100k/u.data', static_data_ratio=0.1)
    ratings, stream, n_items, n_users = load_datasets('datasets/ml-1m/ratings.dat', sep = '::', static_data_ratio=0.1)
    n_items = 3952
    model = MatrixFactorizationModel(item_count=n_items, user_count=n_users, sum_rating=np.sum(ratings[:, 2]),
                                     count_rating=len(ratings[:, 2]))
    sgd = StochasticGradientDescent(model, ratings[:, (0, 1)], ratings[:, 2])
    sgd.train()
    error = []
    i = 0
    cum_error = 0
    buffer_size = 100000
    for item in stream:
        l = np.abs(model.loss(item[0:2], item[2]))
        cum_error += l
        sgd.incremental(item[0:2], item[2], 0.001)
        sgd.update_buffer(item[0:2], item[2])
        if (i % buffer_size == 0) & (i != 0):
            print "{} items process ... scheduling new iteration".format(i)
            sgd.retrain()
        i += 1
        if i % 1000 == 0:
            error += [cum_error / i]

    running_time = time.time() - start_time
    return error, running_time


# error_full, time_full = full()
# plt.plot(np.repeat(error_full, 5000))

error_off_on, time_incremental = offline_online()
# error_continuous, time_continuous = continuous()
# error_velox, time_velox = velox()
# error_naive, time_naive = naive()
# plt.figure(0)
# plt.plot(error_continuous, label='continuous')
# plt.plot(error_off_on, label='offline-online')
# plt.plot(error_velox, label='velox')
# plt.plot(error_naive, label='naive')
# plt.xlabel('training items')
# plt.ylabel('MSE')
# plt.legend(loc='best')
# #
# ind = [1, 2, 3, 4]
# vals = [time_naive, time_incremental, time_continuous, time_velox]
# labels = ['naive', 'incremental', 'continuous', 'Velox']
# plt.figure(1)
# plt.bar(ind, vals, align='center')
# plt.xticks(ind, labels)
# plt.ylabel('time (s)')
