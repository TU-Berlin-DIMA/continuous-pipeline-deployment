from ml_model.model import Model
from sklearn.neural_network import MLPClassifier
from sklearn.metrics import mean_squared_error
import numpy as np
from sklearn.linear_model import SGDClassifier


class NeuralNetworkClassifier(Model):
    def __init__(self):
        Model.__init__(self)
        self.model = MLPClassifier(hidden_layer_sizes=(50,), max_iter=50, alpha=1e-4,
                                   solver='sgd', tol=1e-4, random_state=1,
                                   learning_rate_init=.001)

        # self.model = SGDClassifier()

    def update(self, x, y, learning_rate):
        if len(y) > 0:
            self.model.partial_fit(x, y)

    def batch_update(self, x, y, learning_rate):
        indices = np.arange(len(y))
        np.random.shuffle(indices)
        # print('new training iteration using {} items'.format(len(y)))
        self.model.partial_fit(x[indices, :], y[indices], np.unique(y))

    def loss(self, x, y):
        pass

    def calculate_loss(self, x, y):
        #p = self.model.predict(x)
        return 1-self.model.score(x,y)

    def update_params(self, params):
        pass

    def restart(self):
        pass
