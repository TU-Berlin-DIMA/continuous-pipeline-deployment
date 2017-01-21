from __future__ import division

import matplotlib.pyplot as plt
import numpy as np


def read(path):
    file = open(path, 'r')
    return [float(i) for i in file.readline().split(',')]


def samples(path):
    file = open(path, 'r')
    all = file.readline()
    arrays = all.split(']')
    labels = np.arange(0.1, 1.2, 0.1)
    j = 0
    for arr in arrays:
        vals = [float(i) for i in arr.split(',')]
        plt.plot(vals, label=str(labels[j]))
        j += 1
        # plt.ylim([0.4, 1.6])
    plt.xlabel('Test Cycle', fontsize=14)
    plt.ylabel('Mean Squared Error', fontsize=14)
    plt.legend(loc='best')


samples('results/mnist/nn/sampling/samples.txt')


def fills(path):
    file = open(path, 'r')
    all = file.readline()
    arrays = all.split(']')
    low_sampling = [float(i) for i in arrays[0].split(',')]
    full_sampling = [float(i) for i in arrays[9].split(',')]
    plt.plot(low_sampling, color=[0.0, 0.5, 1])
    plt.text(320, low_sampling[300] + 0.015, 'buffer size = 500')
    plt.plot(full_sampling, color=[0.1, 0, 1])
    plt.text(350, full_sampling[350] - 0.015, 'buffer size = 5000')

    plt.fill_between(np.arange(0, 501), y1=low_sampling, y2=full_sampling, alpha='0.5')
    plt.xlabel('Test Cycle', fontsize=14)
    plt.ylabel('Error Rate', fontsize=14)
    plt.legend(loc='best')
    plt.show()


fills('results/mnist/nn/buffer-size/buffer-size.txt')


def plot_error_rate(root, store=False):
    naive_error = read(root + '/naive-error.txt')
    incremental_error = read(root + '/offline-online.txt')
    continuous_error = read(root + '/continuous-error.txt')
    velox_error = read(root + '/velox-error.txt')
    entire_error = read(root + '/baseline-error.txt') * len(velox_error)
    offline_error = read(root + '/offline-only.txt')

    plt.plot(naive_error, label='naive')
    plt.plot(incremental_error, label='offline+online')
    plt.plot(continuous_error, label='continuous')
    plt.plot(velox_error, label='full-retraining')
    plt.plot(entire_error, label='baseline')
    plt.plot(offline_error, label='offline-only')
    plt.xlabel('Test Cycle', fontsize=14)
    plt.ylabel('Error Rate', fontsize=14)
    plt.legend(loc='best', fontsize='small')
    plt.ylim([0.0, 1.0])
    plt.xlim([0, 500])
    if store:
        plt.savefig(root + '/figure.eps', format='eps', dpi=1000, bbox_inches='tight')


root = 'results/mnist/nn/500'
plot_error_rate(root)


def movielens_buffer_time():
    vals = [i / 60 for i in
            [268.1484389305115,
             268.38905787467957,
             292.9440999031067,
             345.9986479282379,
             399.22615599632263,
             457.0730698108673,
             519.6589460372925,
             643.7678089141846,
             730.1631481647491,
             740.2697830200195]]
    buffer_size = [500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000]
    plt.scatter(buffer_size, vals)
    plt.plot(buffer_size, vals)
    plt.xlabel('Buffer Size', fontsize=14)
    plt.ylabel('Time (Minutes)', fontsize=14)
    plt.show()


def movielens_sampling_time():
    vals = [i / 180 for i in
            [176.61855006217957,
             188.3583481311798,
             199.58596801757812,
             210.64247703552246,
             226.69598603248596,
             235.53669810295105,
             246.04969191551208,
             256.35137605667114,
             274.1467409133911,
             244.51191687583923]
            ]
    buffer_size = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]
    plt.scatter(buffer_size, vals)
    plt.plot(buffer_size, vals)
    plt.xlabel('Sampling Rate', fontsize=14)
    plt.ylabel('Time (Minutes)', fontsize=14)
    plt.show()


def plot_time():
    time_100k = [124.8797, 128.7379, 506.1425, 854.3796, 187.6295]
    time_1M = [138.5513, 235.8159, 4608.0255, 9610.6307, 1825.9909]
    time_MNIST = [2.3790788650512695, 8.481926918029785, 244.62878799438477, 1569.8254868984222, 56.363343954086304]
    ind = [1, 3, 5, 7, 9]

    labels = ['naive', 'offline+online', 'continuous', 'full-Retraining', 'baseline']
    width = 0.40

    plt.bar([i - width for i in ind], time_MNIST, width=width, align='center', alpha=0.9, color='g')
    plt.bar(ind, time_100k, width=width, align='center', alpha=0.9, color='r')
    plt.bar([i + width for i in ind], time_1M, width=width, align='center', alpha=0.9, color='b')

    plt.xticks(ind, labels, fontsize=10)
    plt.yticks([2, 20, 60, 120, 180], fontsize=10)
    plt.ylabel('Time (s) in Log Scale', fontsize=16)
    plt.legend(['MNIST', 'MovieLens100K', 'MovieLens1M'], loc='best', fontsize='small')
    plt.yscale('log', nonposy='clip')


def two_plots():
    f, (ax1, ax2) = plt.subplots(nrows=1, ncols=2, sharex=True, sharey=True)
    labels = ['Naive', 'Offline+Online', 'Continuous', 'Velox', 'Full']
    time_100k = [v / 60.0 for v in [124.8797, 128.7379, 506.1425, 854.3796, 187.6295]]
    ind = [1, 2, 3, 4, 5]
    ax1.bar(ind, time_100k, align='center', )
    time_1M = [v / 60.0 for v in [138.5513, 235.8159, 4608.0255, 9610.6307, 1825.9909]]

    ax2.bar(ind, time_1M, align='center', )
    plt.xticks(ind, labels)
    plt.setp(ax1.get_yticks(), locs=[100, 120, 180])
    f.subplots_adjust(hspace=0.1)

# two_plots()
# movielens_mse('results/movie-lens-100k/5000/', title = 'MovieLens 100k: Mean Squared Error')
# movielens_mse('results/movie-lens-1M/5000/')
