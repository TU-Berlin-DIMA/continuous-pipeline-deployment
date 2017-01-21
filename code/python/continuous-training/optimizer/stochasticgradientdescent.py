from __future__ import print_function
import numpy as np
import math


class StochasticGradientDescent:
    def __init__(self,
                 model, x, y, verbose=False):
        self.model = model
        self.x = x
        self.y = y
        self.verbose = verbose
        self.buffer_x = np.empty([0, self.x.shape[1]], dtype=np.uint64)
        self.buffer_y = np.empty([0, len(self.y)], dtype=np.uint64)

    def train(self, num_iterations=50, learning_rate=0.001):
        ctr = 0
        while ctr < num_iterations:
            self.new_batch(self.x, self.y, learning_rate)
            ctr += 1
            if self.verbose & (ctr % 5 == 0):
                print("{} items processed".format(ctr))

    def new_batch(self, x, y, learning_rate=0.001):
        self.model.batch_update(x, y, learning_rate)
        # indices = np.arange(len(y))
        # np.random.shuffle(indices)
        # for idx in indices:
        #     self.model.update(x[idx], y[idx], learning_rate)

    def incremental(self, x, y, learning_rate):
        self.model.update(x, y, learning_rate)
        self.model.update_params(y)

    def update_buffer(self, x, y):
        self.buffer_x = np.vstack([self.buffer_x, x])
        self.buffer_y = np.append(self.buffer_y, y)

    def merge(self):
        self.x = np.vstack([self.x, self.buffer_x])
        self.y = np.append(self.y, self.buffer_y)
        self.buffer_x = np.empty([0, self.x.shape[1]], dtype=np.uint64)
        self.buffer_y = np.empty([0, len(self.y)], dtype=np.uint64)

    def retrain(self):
        self.model.restart()
        self.merge()
        self.model.set_params(np.mean(self.y))
        self.train()

    def run_iter(self, sample_rate=1.0):
        if sample_rate == 1.0:
            self.merge()
            self.model.set_params(np.mean(self.y))
            self.train(num_iterations=1)
        else:
            high = len(self.y)
            cnt = int(math.floor(sample_rate * high))
            inx = np.random.randint(high, size=cnt)
            sample_x = np.vstack([self.buffer_x, self.x[inx,]])
            sample_y = np.append(self.buffer_y, self.y[inx,])
            self.new_batch(sample_x, sample_y)
            self.merge()
