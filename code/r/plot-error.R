setwd("~/Documents/work/phd/continuous-training-serving/code/spark/continuous-training/results/")

data_velox = read.csv(file = 'velox/error-rate-2017-01-19-09-08.txt', header = FALSE)
data_continuous =read.csv(file = 'continuous/error-rate-2017-01-19-09-18.txt', header = FALSE)
#data_initial =read.csv(file = 'initial/error-rate-2017-01-17-17-30.txt', header = FALSE)


plot(1:dim(data_velox)[1], data_velox$V1, type = 'l' , col = 'red', xlim = c(1,max(dim(data_velox)[1], dim(data_continuous)[1])), ylim = c(0.2,0.5))
lines(1:dim(data_continuous)[1], data_continuous$V1, col ='blue')
#lines(1:dim(data_initial)[1], data_initial$V1, col ='green')
