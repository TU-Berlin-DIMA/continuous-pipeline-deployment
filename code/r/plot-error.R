setwd("~/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/results/criteo-sample/processed/")

data_continuous = read.csv(file = 'continuous/2017-02-06-17-00/error-rates.txt', header = FALSE)
data_velox =read.csv(file = 'velox/2017-02-06-17-17/error-rates.txt', header = FALSE)
data_streaming =read.csv(file = 'online/2017-02-06-17-34/error-rates.txt', header = FALSE)
data_initial =read.csv(file = 'initial-only/2017-02-06-17-52/error-rates.txt', header = FALSE)


plot(1:dim(data_velox)[1], data_velox$V1, type = 'l' , col = 'red', xlim = c(1,max(dim(data_velox)[1], dim(data_velox)[1])), ylim = c(0.2,0.5))
lines(1:dim(data_continuous)[1], data_continuous$V1, col = 'blue')
lines(1:dim(data_streaming)[1], data_streaming$V1, col= 'green')
lines(1:dim(data_initial)[1], data_initial$V1, col= 'darkgoldenrod4')

legend("topright", col = c("red", "blue", "green", "darkgoldenrod4"), lty = 1,legend=c("velox", "continuous", "online", 'initial'))
