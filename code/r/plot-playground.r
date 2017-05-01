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
continuous = read.csv('cover-types/continuous/num-iterations-500/slack-10/offline-step-1.0/online-step-0.1/2017-04-11-11-25/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('cover-types/velox/num-iterations-500/slack-64/offline-step-1.0/online-step-0.1/2017-04-11-11-43/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('cover-types/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-04-11-12-06/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('cover-types/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-04-11-13-10/error-rates.txt', header = FALSE, col.names = 'baseline')

m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))

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
continuous = read.csv('criteo/continuous/num-iterations-500/slack-50/offline-step-1.0/online-step-1.0/2017-03-30-18-53/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('criteo/velox/num-iterations-500/slack-320/offline-step-1.0/online-step-1.0/2017-03-30-19-35/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('criteo/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-30-20-17/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('criteo/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-30-20-17/error-rates.txt', header = FALSE, col.names = 'baseline')

m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
continuous = rbind(continuous, data.frame(continuous = rep(tail(continuous, n = 1), m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(tail(velox, n = 1), m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(tail(baseline), m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(tail(baselinePlus, n = 1), m - nrow(baselinePlus))))

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
continuous = read.csv('sea/continuous/num-iterations-500/slack-10/offline-step-1.0/online-step-0.05/2017-04-12-10-35/error-rates.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('sea/velox/num-iterations-500/slack-64/offline-step-1.0/online-step-0.05/2017-04-12-10-07/error-rates.txt', header = FALSE, col.names = 'velox')
baselinePlus = read.csv('sea/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.05/2017-04-11-23-10/error-rates.txt', header = FALSE, col.names = 'baselinePlus')
baseline= read.csv('sea/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-0.05/2017-04-11-23-20/error-rates.txt', header = FALSE, col.names = 'baseline')

m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous, 
                velox = velox, 
                baseline = baseline,
                baselinePlus = baselinePlus)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error") 


#URL 
# step size = 0.5
#continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-200/offline-step-1.0/online-step-0.01/2017-04-25-11-50/error-rates.txt', header = FALSE, col.names = 'continuous')
# step size = 0.1
#continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-200/offline-step-1.0/online-step-0.01/2017-04-25-09-36/error-rates.txt', header = FALSE, col.names = 'continuous')
# step size = 1.0
#continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-200/offline-step-1.0/online-step-0.01/2017-04-25-10-48/error-rates.txt', header = FALSE, col.names = 'continuous')
# step size = 0.1 full
#continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-200/offline-step-1.0/online-step-0.01/2017-04-26-22-38/error-rates.txt', header = FALSE, col.names = 'continuous')
# step size = 0.2
#continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-200/offline-step-1.0/online-step-0.01/2017-04-25-13-35/error-rates.txt', header = FALSE, col.names = 'continuous')
# step size = 0.4
#continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-200/offline-step-1.0/online-step-0.01/2017-04-27-12-09/error-rates.txt', header = FALSE, col.names = 'continuous')
# step size = 0.3
#continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-200/offline-step-1.0/online-step-0.01/2017-04-27-17-17/error-rates.txt', header = FALSE, col.names = 'continuous')
# step size = 0.1
#continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-200/offline-step-1.0/online-step-0.05/2017-04-28-00-02/error-rates.txt', header = FALSE, col.names = 'continuous')
# step size = 0.2
#continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-200/offline-step-1.0/online-step-0.01/', header = FALSE, col.names = 'continuous')

continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-40/offline-step-1.0/online-step-0.1/continuous-step-0.2/2017-05-01-10-57/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('url-reputation/velox/num-iterations-500/slack-1280/offline-step-1.0/online-step-0.1/2017-04-12-02-46/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('url-reputation/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-04-24-05-46/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('url-reputation/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-04-24-12-11/error-rates.txt', header = FALSE, col.names = 'baseline')

m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
#m = 1600
#continuous = continuous[1:m,]
#velox = velox[1:m,]
#baselinePlus = baselinePlus[1:m,]
#baseline = baseline[1:m,]
continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))


df = data.frame(time = 1:m,
                continuous = continuous, 
                velox = velox, 
                baseline = baseline,
                baselinePlus = baselinePlus)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Error Rate") 



#HIGGS
continuous = read.csv('higgs/continuous/num-iterations-1000/slack-100/offline-step-1.0/online-step-0.1/2017-04-11-17-04/error-rates.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('higgs/velox/num-iterations-500/slack-320/offline-step-1.0/online-step-0.1/2017-04-10-17-17/error-rates.txt', header = FALSE, col.names = 'velox')
baselinePlus = read.csv('higgs/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-04-10-18-55/error-rates.txt', header = FALSE, col.names = 'baselinePlus')
baseline = read.csv('higgs/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-04-11-10-31/error-rates.txt', header = FALSE, col.names = 'baseline')

m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous,
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error")



#Adult
continuous = read.csv('adult/continuous/num-iterations-500/slack-10/offline-step-1.0/online-step-0.1/2017-04-11-17-19/error-rates.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('adult/velox/num-iterations-500/slack-64/offline-step-1.0/online-step-0.1/2017-04-11-17-50/error-rates.txt', header = FALSE, col.names = 'velox')
baselinePlus = read.csv('adult/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-04-11-17-35/error-rates.txt', header = FALSE, col.names = 'baselinePlus')
baseline = read.csv('adult/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-04-11-18-08/error-rates.txt', header = FALSE, col.names = 'baseline')

m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous,
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error")

#SUSY SAMPLE
continuous = read.csv('susy-sample/continuous/num-iterations-300/slack-2/offline-step-1.0/online-step-1.0/2017-03-23-16-53/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('susy-sample/velox/num-iterations-300/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-16-20/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('susy-sample/baseline-plus/num-iterations-300/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-16-24/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline = read.csv('susy-sample/baseline/num-iterations-300/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-16-26/error-rates.txt', header = FALSE, col.names = 'baseline')

m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous,
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error")


#SUSY Cluster
continuous = read.csv('susy/continuous/num-iterations-500/slack-20/offline-step-1.0/online-step-1.0/2017-03-30-11-48/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('susy/velox/num-iterations-500/slack-400/offline-step-1.0/online-step-1.0/2017-03-28-21-07/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('susy/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-03-28-18-55/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline = read.csv('susy/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-29-09-16/error-rates.txt', header = FALSE, col.names = 'baseline')

m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous,
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error") 

#SUSY-SAMPLE-GRIDSEARCH
continuous05= read.csv('susy-sample/continuous/num-iterations-300/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-16-43/error-rates.txt', header = FALSE, col.names = 'continuous05')
continuous02 = read.csv('susy-sample/continuous/num-iterations-300/slack-2/offline-step-1.0/online-step-1.0/2017-03-23-16-53/error-rates.txt', header = FALSE, col.names = 'continuous02')
continuous08 = read.csv('susy-sample/continuous/num-iterations-300/slack-8/offline-step-1.0/online-step-1.0/2017-03-23-16-56/error-rates.txt', header = FALSE, col.names = 'continuous08')
continuous10 = read.csv('susy-sample/continuous/num-iterations-300/slack-10/offline-step-1.0/online-step-1.0/2017-03-23-16-58/error-rates.txt', header = FALSE, col.names = 'continuous10')

m = max(
        nrow(continuous05), 
      
        nrow(continuous02), 
       
        nrow(continuous08), 
        
        nrow(continuous10))

continuous05 = rbind(continuous05, data.frame(continuous05 = rep(NA, m - nrow(continuous05))))

continuous02 = rbind(continuous02, data.frame(continuous02 = rep(NA, m - nrow(continuous02))))

continuous08 = rbind(continuous08, data.frame(continuous08 = rep(NA, m - nrow(continuous08))))

continuous10 = rbind(continuous10, data.frame(continuous10 = rep(NA, m - nrow(continuous10))))


df = data.frame(time = 1:nrow(continuous05),
                continuous05 = continuous05,
                continuous02 = continuous02,
                continuous08 = continuous08,
                continuous10 = continuous10)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error") 



