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
continuous = read.csv('cover-types/continuous/num-iterations-300/slack-4/offline-step-1.0/online-step-1.0/2017-03-22-14-31/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('cover-types/velox/num-iterations-300/slack-32/offline-step-1.0/online-step-1.0/2017-03-22-14-35/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('cover-types/baseline-plus/num-iterations-300/slack-none/offline-step-1.0/online-step-1.0/2017-03-22-14-39/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('cover-types/baseline/num-iterations-300/slack-none/offline-step-1.0/online-step-1.0/2017-03-22-14-42/error-rates.txt', header = FALSE, col.names = 'baseline')

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
continuous = read.csv('criteo/continuous/batch-2-slack-40-incremental-true-error-cumulative-prequential-1.0-200/2017-02-27-21-46/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('criteo/velox/batch-2-slack-400-incremental-true-error-cumulative-prequential-1.0-200/2017-02-27-23-11/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('criteo/baseline-plus/batch-2-slack-none-incremental-true-error-cumulative-prequential-1.0-200/2017-02-28-10-21/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('criteo/baseline/batch-2-slack-none-incremental-false-error-cumulative-prequential-1.0-200/2017-02-28-00-49/error-rates.txt', header = FALSE, col.names = 'baseline')

m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
continuous = rbind(continuous, data.frame(continuous = rep(tail(continuous, n = 1), m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(tail(velox, n = 1), m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(tail(baseline), m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(tail(baselinePlus, n = 1), m - nrow(baselinePlus))))

df = data.frame(time = 1:nrow(velox),
                continuous = continuous, 
                velox = velox,
                baseline = baseline)
                #baselinePlus = baselinePlus)

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
continuous = read.csv('sea/continuous/num-iterations-1000/slack-5/offline-step-1.0/online-step-0.1/2017-03-23-13-42/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('sea/velox/num-iterations-1000/slack-32/offline-step-1.0/online-step-0.05/2017-03-23-13-21/error-rates.txt', header = FALSE, col.names = 'velox')

baselineBase = read.csv('sea/baseline-plus/num-iterations-300/slack-none/offline-step-1.0/online-step-1.0/2017-03-22-18-49/error-rates.txt', header = FALSE, col.names = 'baseline-plus')

baseline= read.csv('sea/baseline/num-iterations-300/slack-none/offline-step-1.0/online-step-0.01/2017-03-23-11-24/error-rates.txt', header = FALSE, col.names = 'baseline')

m = max(nrow(continuous), nrow(velox), nrow(baselineBase),nrow(baseline))
continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
baselineBase = rbind(baselineBase, data.frame(baselineBase = rep(NA, m - nrow(baselineBase))))
baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous, 
                velox = velox,
                baselineBase = baselineBase,
                baseline=baseline)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error")



#URL 
continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-50/offline-step-1.0/online-step-0.1/2017-03-28-13-50/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('url-reputation/velox/num-iterations-500/slack-400/offline-step-1.0/online-step-0.1/2017-03-25-20-58/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('url-reputation/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-03-26-14-27/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('url-reputation/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-03-28-18-05/error-rates.txt', header = FALSE, col.names = 'baseline')

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


#URL SAMPLE 
continuous = read.csv('url-reputation-sample/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-0.1/2017-03-28-08-05/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('url-reputation-sample/velox/num-iterations-300/slack-75/offline-step-1.0/online-step-0.01/2017-03-28-10-02/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('url-reputation-sample/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.01/2017-03-28-09-01/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline = read.csv('url-reputation-sample/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-03-28-08-28/error-rates.txt', header = FALSE, col.names = 'baseline')

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



#HIGGS SAMPLE
continuous = read.csv('higgs-sample/continuous/num-iterations-300/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-15-37/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('higgs-sample/velox/num-iterations-300/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-15-44/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('higgs-sample/baseline-plus/num-iterations-300/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-16-00/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline = read.csv('higgs-sample/baseline/num-iterations-300/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-16-04/error-rates.txt', header = FALSE, col.names = 'baseline')

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



#HIGGS
continuous = read.csv('higgs/continuous/num-iterations-300/slack-30/offline-step-1.0/online-step-1.0/2017-03-23-14-40/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('higgs/velox/num-iterations-300/slack-600/offline-step-1.0/online-step-1.0/2017-03-23-13-14/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('higgs/baseline-plus/num-iterations-300/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-15-59/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline = read.csv('higgs/baseline-plus/num-iterations-300/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-15-59/error-rates.txt', header = FALSE, col.names = 'baseline')

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
continuous = read.csv('susy/continuous/num-iterations-500/slack-50/offline-step-1.0/online-step-0.1/2017-03-28-17-46/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('susy/velox/num-iterations-500/slack-320/offline-step-1.0/online-step-1.0/2017-03-24-00-07/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('susy/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-03-28-18-55/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline = read.csv('susy/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-24-09-04/error-rates.txt', header = FALSE, col.names = 'baseline')

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



