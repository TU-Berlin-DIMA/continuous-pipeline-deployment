setwd("~/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/sea/raw/")

library(ggplot2)
library(reshape)

data = read.csv(file = 'sea.data', header= FALSE)
data[,-c(1)] = scale(data[,-c(1)])

write.table(data, file = 'sea.scaled.data', col.names = FALSE, row.names = FALSE, sep = ',')