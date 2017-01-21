from __future__ import division

import numpy as np

from ml_model.model import Model


class MatrixFactorizationModel(Model):
    def __init__(self,
                 user_count,
                 item_count,
                 n_factors=40,
                 item_fact_reg=0.0,
                 user_fact_reg=0.0,
                 item_bias_reg=0.0,
                 user_bias_reg=0.0,
                 sum_rating=0.0,
                 count_rating=0):
        Model.__init__(self)
        self.n_factors = n_factors
        self.item_fact_reg = item_fact_reg
        self.user_fact_reg = user_fact_reg
        self.item_bias_reg = item_bias_reg
        self.user_bias_reg = user_bias_reg
        self.user_count = user_count
        self.item_count = item_count

        # initialize model parameters
        self.user_vecs = np.random.normal(scale=1. / self.n_factors, size=(self.user_count, self.n_factors))
        self.item_vecs = np.random.normal(scale=1. / self.n_factors, size=(self.item_count, self.n_factors))
        self.user_bias = np.zeros(self.user_count)
        self.item_bias = np.zeros(self.item_count)
        # np.mean(self.ratings[:,2])
        self.sum_rating = sum_rating
        self.count_rating = count_rating
        if count_rating == 0:
            self.global_bias = 0
        else:
            self.global_bias = sum_rating / count_rating

    def restart(self):
        self.user_vecs = np.random.normal(scale=1. / self.n_factors, size=(self.user_count, self.n_factors))
        self.item_vecs = np.random.normal(scale=1. / self.n_factors, size=(self.item_count, self.n_factors))
        self.user_bias = np.zeros(self.user_count)
        self.item_bias = np.zeros(self.item_count)

    def batch_update(self, x, y, learning_rate):
        indices = np.arange(len(y))
        np.random.shuffle(indices)
        for idx in indices:
            self.update(x[idx], y[idx], learning_rate)

    def update(self, item, rating, learning_rate):
        # calculate loss
        e = self.loss(item, rating)
        # Update biases
        self.update_bias(item, e, learning_rate)
        # Update latent factors
        self.update_factors(item, e, learning_rate)

    def loss(self, item, r):
        return r - self.predict(item[0], item[1])  # error

    def parameters(self):
        return self.user_vecs, self.item_vecs

    def predict(self, u, i):
        prediction = self.global_bias + self.user_bias[u] + self.item_bias[i]
        prediction += self.user_vecs[u, :].dot(self.item_vecs[i, :].T)
        return prediction

    def update_bias(self, item, e, learning_rate):
        u, i = item[0], item[1]
        self.user_bias[u] += learning_rate * (e - self.user_bias_reg * self.user_bias[u])
        self.item_bias[i] += learning_rate * (e - self.item_bias_reg * self.item_bias[i])

    def update_factors(self, item, e, learning_rate):
        u, i = item[0], item[1]
        self.user_vecs[u] += learning_rate * (e * self.item_vecs[i] - self.user_fact_reg * self.user_vecs[u])
        self.item_vecs[i] += learning_rate * (e * self.user_vecs[u] - self.item_fact_reg * self.item_vecs[i])

    def calculate_loss(self, x, y):
        err = 0
        for (user, item), rating in zip(x[:, (0, 1)], y):
            prediction = self.predict(user, item)
            err += np.math.pow((prediction - rating), 2)
        return err / len(x)

    def set_params(self, params):
        self.global_bias = params

    def update_params(self, params):
        self.sum_rating += params
        self.count_rating += 1
        self.global_bias = self.sum_rating / self.count_rating
