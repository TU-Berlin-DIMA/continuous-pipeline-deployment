setwd("~/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/results/")

data_continuous = read.csv(file = 'cover-types/continuous/2017-01-24-17-51/error-rates.txt', header = FALSE)
data_velox =read.csv(file = 'cover-types/velox/2017-01-24-17-36/error-rates.txt', header = FALSE)
data_streaming =read.csv(file = 'cover-types/online/2017-01-24-17-56/error-rates.txt', header = FALSE)


plot(1:dim(data_continuous)[1], data_continuous$V1, type = 'l' , col = 'red', xlim = c(1,max(dim(data_continuous)[1], dim(data_continuous)[1])), ylim = c(0.2,0.5))
#lines(1:dim(data_continuous)[1], data_continuous$V1)
lines(1:dim(data_streaming)[1], data_streaming$V1, col= 'blue')


