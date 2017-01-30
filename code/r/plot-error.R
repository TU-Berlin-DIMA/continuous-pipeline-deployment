setwd("~/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/results/")

data_continuous = read.csv(file = 'sea/continuous/2017-01-26-15-49/error-rates.txt', header = FALSE)
data_velox =read.csv(file = 'sea/velox/2017-01-26-16-32/error-rates.txt', header = FALSE)
data_streaming =read.csv(file = 'sea/online/2017-01-26-15-56/error-rates.txt', header = FALSE)
data_initial =read.csv(file = 'sea/initial-only/2017-01-26-15-51/error-rates.txt', header = FALSE)


plot(1:dim(data_velox)[1], data_velox$V1, type = 'l' , col = 'red', xlim = c(1,max(dim(data_velox)[1], dim(data_velox)[1])), ylim = c(0.2,0.5))
lines(1:dim(data_continuous)[1], data_continuous$V1, col = 'blue')
lines(1:dim(data_streaming)[1], data_streaming$V1, col= 'green')
lines(1:dim(data_initial)[1], data_initial$V1, col= 'darkgoldenrod4')

legend("topright", col = c("red", "blue", "green", "darkgoldenrod4"), lty = 1,legend=c("velox", "continuous", "online", 'initial'))
