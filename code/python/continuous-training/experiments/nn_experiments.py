from __future__ import division

import math
import random
import time

import numpy as np
import scipy.io as sio
from sklearn.datasets import fetch_mldata
from sklearn.neural_network import MLPClassifier

from classifier.neural_network import NeuralNetworkClassifier
from optimizer.stochasticgradientdescent import StochasticGradientDescent


def rgb2gray(rgb):
    return np.dot(rgb[..., :3], [0.299, 0.587, 0.114]).flatten()


def unpickle(file):
    import cPickle
    fo = open(file, 'rb')
    dict = cPickle.load(fo)
    fo.close()
    return dict


def trysvhn():
    path = 'datasets/svhn'
    train_data = sio.loadmat(path + '/train_32x32.mat')
    test_data = sio.loadmat(path + '/test_32x32.mat')

    x_train = train_data['X']
    y = train_data['y']

    x_test = test_data['X']
    y_test = test_data['y']

    x_train_fixed = np.empty([x_train.shape[3], 32 * 32 * 3], dtype=np.uint64)
    x_test_fixed = np.empty([x_test.shape[3], 32 * 32 * 3], dtype=np.uint64)

    for i in range(len(y)):
        x_train_fixed[i, 0:1024] = x_train[:, :, 0, i].flatten(order='C')
        x_train_fixed[i, 1024:2048] = x_train[:, :, 1, i].flatten(order='C')
        x_train_fixed[i, 2048:3072] = x_train[:, :, 2, i].flatten(order='C')

    for i in range(len(y_test)):
        x_test_fixed[i, 0:1024] = x_test[:, :, 0, i].flatten(order='C')
        x_test_fixed[i, 1024:2048] = x_test[:, :, 1, i].flatten(order='C')
        x_test_fixed[i, 2048:3072] = x_test[:, :, 2, i].flatten(order='C')

    model = MLPClassifier(hidden_layer_sizes=(100,), max_iter=50, alpha=1e-4,
                          solver='sgd', verbose=10, tol=1e-4, random_state=1,
                          learning_rate_init=.001)

    model.fit(x_train_fixed, y.ravel())
    print(model.score(x_test_fixed, y_test.ravel()))


def loadsvhn(path, static_data_ratio=0.1):
    train_data = sio.loadmat(path + '/train_32x32.mat')
    test_data = sio.loadmat(path + '/test_32x32.mat')

    # rescale the data, use the traditional train/test split
    x_train = train_data['X']
    y_train = train_data['y']

    x_test = test_data['X']
    y_test = test_data['y']

    x_train_gray = np.empty([x_train.shape[3], 32 * 32], dtype=np.uint64)
    x_test_gray = np.empty([x_test.shape[3], 32 * 32], dtype=np.uint64)

    for i in range(x_train.shape[3]):
        x_train_gray[i, :] = rgb2gray(x_train[:, :, :, i])

    for i in range(x_test.shape[3]):
        x_test_gray[i, :] = rgb2gray(x_test[:, :, :, i])

    initial_data_size = int(static_data_ratio * x_train.shape[3])

    x_train_initial = x_train_gray[0:initial_data_size, :]
    y_train_initial = y_train[0:initial_data_size].ravel()

    x_train_stream = x_train_gray[initial_data_size:, :]
    y_train_stream = y_train[initial_data_size:].ravel()

    vals = random.sample(np.arange(0, len(y_test)), 5000)

    return x_train_initial, y_train_initial.ravel(), x_train_stream, y_train_stream, x_test_gray[vals,], y_test[
        vals].ravel()


def loadmnist(static_data_ratio=0.1):
    mnist = fetch_mldata("MNIST original")
    # rescale the data, use the traditional train/test split
    X, y = mnist.data / 255., mnist.target
    indices = np.arange(y.shape[0])
    np.random.seed(seed=42)
    np.random.shuffle(indices)
    X = X[indices, :]
    y = y[indices]
    x_train, x_test = X[:69500], X[69500:]
    y_train, y_test = y[:69500], y[69500:]

    initial_data_size = int(static_data_ratio * x_train.shape[0])

    x_train_initial = x_train[0:initial_data_size, :]
    y_train_initial = y_train[0:initial_data_size]

    x_train_stream = x_train[initial_data_size:, :]
    y_train_stream = y_train[initial_data_size:]

    # vals = random.sample(np.arange(0, len(y_test)), 5000)

    return x_train_initial, y_train_initial, x_train_stream, y_train_stream, x_test, y_test


def loadcifar(path='datasets/cifar-10/', static_data_ratio=0.1):
    d1 = unpickle(path + 'data_batch_1')
    test = unpickle(path + 'test_batch')
    d2 = unpickle(path + 'data_batch_2')
    d3 = unpickle(path + 'data_batch_3')
    d4 = unpickle(path + 'data_batch_4')
    d5 = unpickle(path + 'data_batch_5')
    model = MLPClassifier(hidden_layer_sizes=(100,), max_iter=50, alpha=1e-4,
                          solver='sgd', verbose=10, tol=1e-4, random_state=1,
                          learning_rate_init=.001)
    x = np.vstack([d1['data'], d2['data'], d3['data'], d4['data'], d5['data']])
    y = np.append(d1['labels'], [[d2['labels'], d3['labels'], d4['labels'], d5['labels']]])
    model.fit(x, y)
    print(model.score(test['data'], test['labels']))


def simple():
    x, y, x_s, y_s, x_test, y_test = loadsvhn(path='../datasets/svhn/', static_data_ratio=1.0)
    model = NeuralNetworkClassifier()
    sgd = StochasticGradientDescent(model, x, y)
    sgd.train(num_iterations=50, learning_rate=0.001)
    print (model.calculate_loss(x_test, y_test))

# start with a batch then continue with online learning
def naive():
    start_time = time.time()
    # x, y, x_s, y_s, x_test, y_test = load_datasets(path='datasets/svhn/', static_data_ratio=0.1)
    x, y, x_s, y_s, x_test, y_test = loadmnist(static_data_ratio=0.0)

    model = NeuralNetworkClassifier()
    sgd = StochasticGradientDescent(model, x, y)
    #sgd.train(num_iterations=50, learning_rate=0.001)

    error = []
    test_increment = math.floor(len(y_s) / len(y_test))
    i = 0
    idx = 2
    while i <= len(y_s):
        end_of_batch = i + test_increment
        if i + test_increment >= len(y_s):
            end_of_batch = len(y_s)
        sgd.incremental(x_s[i: end_of_batch, ], y_s[i: end_of_batch], 0.001)
        if idx < len(y_test):
            error_rate = model.calculate_loss(x_test[0:idx], y_test[0:idx])
        else:
            error_rate = model.calculate_loss(x_test, y_test)
        error += [error_rate]
        print("{} items process".format(i))
        i += test_increment
        idx += 1

    error_rate = model.calculate_loss(x_test, y_test)
    error += [error_rate]

    running_time = time.time() - start_time
    return error, running_time

# start with a batch then continue with online learning
def offline_only():
    start_time = time.time()
    # x, y, x_s, y_s, x_test, y_test = load_datasets(path='datasets/svhn/', static_data_ratio=0.1)
    x, y, x_s, y_s, x_test, y_test = loadmnist(static_data_ratio=0.1)

    model = NeuralNetworkClassifier()
    sgd = StochasticGradientDescent(model, x, y)
    sgd.train(num_iterations=50, learning_rate=0.001)

    error = []
    test_increment = math.floor(len(y_s) / len(y_test))
    i = 0
    idx = 2
    while i <= len(y_s):
        end_of_batch = i + test_increment
        if i + test_increment >= len(y_s):
            end_of_batch = len(y_s)
        #sgd.incremental(x_s[i: end_of_batch, ], y_s[i: end_of_batch], 0.001)
        if idx < len(y_test):
            error_rate = model.calculate_loss(x_test[0:idx], y_test[0:idx])
        else:
            error_rate = model.calculate_loss(x_test, y_test)
        error += [error_rate]
        print("{} items process".format(i))
        i += test_increment
        idx += 1

    error_rate = model.calculate_loss(x_test, y_test)
    error += [error_rate]

    running_time = time.time() - start_time
    return error, running_time



# start with a batch then continue with online learning
def offline_online():
    start_time = time.time()
    # x, y, x_s, y_s, x_test, y_test = load_datasets(path='datasets/svhn/', static_data_ratio=0.1)
    x, y, x_s, y_s, x_test, y_test = loadmnist(static_data_ratio=0.1)

    model = NeuralNetworkClassifier()
    sgd = StochasticGradientDescent(model, x, y)
    sgd.train(num_iterations=50, learning_rate=0.001)

    error = []
    test_increment = math.floor(len(y_s) / len(y_test))
    i = 0
    idx = 2
    while i <= len(y_s):
        end_of_batch = i + test_increment
        if i + test_increment >= len(y_s):
            end_of_batch = len(y_s)
        sgd.incremental(x_s[i: end_of_batch, ], y_s[i: end_of_batch], 0.001)
        if idx < len(y_test):
            error_rate = model.calculate_loss(x_test[0:idx], y_test[0:idx])
        else:
            error_rate = model.calculate_loss(x_test, y_test)
        error += [error_rate]
        print("{} items process".format(i))
        i += test_increment
        idx += 1

    error_rate = model.calculate_loss(x_test, y_test)
    error += [error_rate]

    running_time = time.time() - start_time
    return error, running_time


def continuous(buffer_size = 500, sample_rate = 1.0):
    start_time = time.time()
    # x, y, x_s, y_s, x_test, y_test = load_datasets(path='datasets/svhn/', static_data_ratio=0.1)
    x, y, x_s, y_s, x_test, y_test = loadmnist(static_data_ratio=0.1)

    model = NeuralNetworkClassifier()
    sgd = StochasticGradientDescent(model, x, y)
    sgd.train(num_iterations=50, learning_rate=0.001)
    error = []
    test_increment = math.floor(len(y_s) / len(y_test))
    i = 0
    idx = 2
    while i < len(y_s):
        sgd.incremental(np.array([x_s[i]]), np.array([y_s[i]]), 0.001)
        sgd.update_buffer(x_s[i], y_s[i])
        # print("{} items process".format(i))
        i += 1
        if i % test_increment == 0:
            if idx < len(y_test):
                error_rate = model.calculate_loss(x_test[0:idx], y_test[0:idx])
            else:
                error_rate = model.calculate_loss(x_test, y_test)
            error += [error_rate]
        if (i % buffer_size == 0) & (i != 0):
            print("{} items process ... scheduling new iteration".format(i))
            sgd.run_iter(sample_rate=sample_rate)
        idx += 1

    error_rate = model.calculate_loss(x_test, y_test)
    error += [error_rate]

    running_time = time.time() - start_time
    return error, running_time


def velox():
    start_time = time.time()
    # x, y, x_s, y_s, x_test, y_test = load_datasets(path='datasets/svhn/', static_data_ratio=0.1)
    x, y, x_s, y_s, x_test, y_test = loadmnist(static_data_ratio=0.1)

    model = NeuralNetworkClassifier()
    sgd = StochasticGradientDescent(model, x, y)
    sgd.train(num_iterations=50, learning_rate=0.001)
    buffer_size = 10000
    error = []
    test_increment = math.floor(len(y_s) / len(y_test))
    i = 0
    idx = 2
    while i < len(y_s):
        sgd.incremental(np.array([x_s[i]]), np.array([y_s[i]]), 0.001)
        sgd.update_buffer(x_s[i], y_s[i])
        # print("{} items process".format(i))
        i += 1
        if i % test_increment == 0:
            if idx < len(y_test):
                error_rate = model.calculate_loss(x_test[0:idx], y_test[0:idx])
            else:
                error_rate = model.calculate_loss(x_test, y_test)
            error += [error_rate]
        if i % buffer_size == 0:
            print("{} items process ... scheduling new iteration".format(i))
            sgd.retrain()
        idx += 1

    error_rate = model.calculate_loss(x_test, y_test)
    error += [error_rate]

    running_time = time.time() - start_time
    return error, running_time


def full():
    start_time = time.time()
    # x, y, x_s, y_s, x_test, y_test = load_datasets(path='datasets/svhn/', static_data_ratio=0.1)
    x, y, x_s, y_s, x_test, y_test = loadmnist(static_data_ratio=1.0)

    model = NeuralNetworkClassifier()
    sgd = StochasticGradientDescent(model, x, y)
    sgd.train(num_iterations=50, learning_rate=0.001)
    error = []
    error_rate = model.calculate_loss(x_test, y_test)
    error += [error_rate]
    running_time = time.time() - start_time
    return error, running_time


# continuous_error, continuous_time = continuous()
# offon_error, offon_time = offline_online()
# full_error, full_time = full()
# velox_error, velox_time = velox()
# offline_error = offline_only()
# naive_error ,naive_time = naive()

error_buffers = []
times_buffers = []
for s in np.arange(500, 5500, 500):
    print("Buffer Size {}".format(s))
    error_continuous, time_continuous = continuous(buffer_size=s)
    error_buffers += [error_continuous]
    times_buffers += [time_continuous]
