setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)

loadData <- function(file){
  library(readr)
  library(stringr)
  data = read_file(file)
  data = str_replace_all(data, " ", "")
  data = strsplit(data, ',')
  return(as.numeric(data[[1]]))
}
# Plot cover type Results
continuous = read.csv('criteo-sample/continuous/batch-1-slack-4-incremental-true-error-cumulative-prequential-1.0-200/2017-02-26-14-40/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('criteo-sample/velox/batch-1-slack-20-incremental-true-error-cumulative-prequential-1.0-200/2017-02-26-14-36/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('criteo-sample/baseline-plus/batch-1-slack-none-incremental-true-error-cumulative-prequential-1.0-200/2017-02-26-14-43/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('criteo-sample/baseline/batch-1-slack-none-incremental-false-error-cumulative-prequential-1.0-200/2017-02-26-15-08/error-rates.txt', header = FALSE, col.names = 'baseline')

#m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
#continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
#velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
#baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
#baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous, 
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error") 


# Plot Criteo cluster
continuous = read.csv('criteo/continuous/batch-2-slack-20-incremental-true-error-cumulative-prequential-1.0-200/2017-02-26-17-23/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('criteo/velox/batch-2-slack-400-incremental-true-error-cumulative-prequential-1.0-200/2017-02-26-15-24/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('criteo/baseline-plus/batch-1-slack-none-incremental-true-error-cumulative-prequential-1.0-200/2017-02-26-19-39/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('criteo/baseline/batch-1-slack-none-incremental-false-error-cumulative-prequential-1.0-200/2017-02-26-21-18/error-rates.txt', header = FALSE, col.names = 'baseline')

#m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
#continuous = rbind(continuous, data.frame(continuous = rep(tail(continuous, n = 1), m - nrow(continuous))))
#velox = rbind(velox, data.frame(velox = rep(tail(velox, n = 1), m - nrow(velox))))
#baseline = rbind(baseline, data.frame(baseline = rep(tail(baseline), m - nrow(baseline))))
#baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(tail(baselinePlus, n = 1), m - nrow(baselinePlus))))

df = data.frame(time = 1:nrow(velox),
                continuous = continuous, 
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

ml = melt(df, id.vars = 'time')
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error") 


# Plot Criteo sample cluster
continuous = read.csv('criteo-sample/continuous/batch-1-slack-2-incremental-true-error-cumulative-prequential-1.0-150/2017-02-28-22-48/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('criteo-sample/velox/batch-1-slack-33-incremental-true-error-cumulative-prequential-1.0-500/2017-02-28-11-26/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('criteo-sample/baseline-plus/batch-1-slack-none-incremental-true-error-cumulative-prequential-1.0-500/2017-02-28-11-35/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('criteo-sample/baseline/batch-1-slack-none-incremental-false-error-cumulative-prequential-1.0-500/2017-02-28-11-38/error-rates.txt', header = FALSE, col.names = 'baseline')

#m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
#continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
#velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
#baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
#baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous, 
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error")


# Plot SEA
continuous = read.csv('sea/continuous/batch-1-slack-2-incremental-true-error-cumulative-prequential-1.0-200/2017-02-28-22-56/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('sea/velox/batch-1-slack-33-incremental-true-error-cumulative-prequential-1.0-200/2017-02-28-23-01/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('sea/baseline-plus/batch-1-slack-none-incremental-true-error-cumulative-prequential-1.0-200/2017-02-28-23-09/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('sea/baseline/batch-1-slack-none-incremental-false-error-cumulative-prequential-1.0-200/2017-02-28-23-07/error-rates.txt', header = FALSE, col.names = 'baseline')

#m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
#continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
#velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
#baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
#baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous, 
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error")
