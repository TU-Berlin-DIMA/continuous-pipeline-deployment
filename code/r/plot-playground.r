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
continuous = read.csv('cover-types/continuous/batch-1/slack-10/incremental-true/error-cumulative-prequential/num-iterations-200/2017-03-20-15-01/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('cover-types/velox/batch-1/slack-40/incremental-true/error-cumulative-prequential/num-iterations-200/2017-03-20-13-46/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('cover-types/baseline-plus/batch-1/slack-none/incremental-true/error-cumulative-prequential/num-iterations-200/2017-03-20-13-52/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('cover-types/baseline/batch-1/slack-none/incremental-false/error-cumulative-prequential/num-iterations-200/2017-03-20-13-54/error-rates.txt', header = FALSE, col.names = 'baseline')

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

#m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
#continuous = rbind(continuous, data.frame(continuous = rep(tail(continuous, n = 1), m - nrow(continuous))))
#velox = rbind(velox, data.frame(velox = rep(tail(velox, n = 1), m - nrow(velox))))
#baseline = rbind(baseline, data.frame(baseline = rep(tail(baseline), m - nrow(baseline))))
#baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(tail(baselinePlus, n = 1), m - nrow(baselinePlus))))

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



#URL 
continuous = read.csv('url-reputation/continuous/batch-5-slack-50-incremental-true-error-cumulative-prequential-100/2017-03-10-16-24/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('url-reputation/velox/batch-3-slack-600-incremental-true-error-cumulative-prequential-100/2017-03-11-13-40/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('url-reputation/baseline-plus/batch-3-slack-none-incremental-true-error-cumulative-prequential-100/2017-03-12-16-34/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('url-reputation/baseline/batch-2-slack-none-incremental-false-error-cumulative-prequential-100/2017-03-12-15-02/error-rates.txt', header = FALSE, col.names = 'baseline')

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
continuous = read.csv('url-reputation-sample/continuous/batch-2/slack-4/incremental-true/error-cumulative-prequential/num-iterations-200/2017-03-15-17-19/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('url-reputation-sample/velox/batch-1/slack-70/incremental-true/error-cumulative-prequential/num-iterations-200/2017-03-14-15-23/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('url-reputation-sample/baseline-plus/batch-1/slack-none/incremental-true/error-cumulative-prequential/num-iterations-200/2017-03-15-17-08/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline = read.csv('url-reputation-sample/baseline/batch-1/slack-none/incremental-false/error-cumulative-prequential/num-iterations-200/2017-03-14-15-14/error-rates.txt', header = FALSE, col.names = 'baseline')

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
continuous = read.csv('higgs-sample/continuous/num-iterations-300/slack-4/offline-step-1.0/online-step-1.0/2017-03-22-14-05/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('higgs-sample/velox/num-iterations-300/slack-50/offline-step-1.0/online-step-1.0/2017-03-21-21-01/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('higgs-sample/baseline-plus/num-iterations-300/slack-none/offline-step-1.0/online-step-1.0/2017-03-21-21-15/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline = read.csv('higgs-sample/baseline/num-iterations-300/slack-none/offline-step-1.0/online-step-1.0/2017-03-21-21-07/error-rates.txt', header = FALSE, col.names = 'baseline')

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


#HIGGS-SAMPLE-GRIDSEARCH
continuous02 = read.csv('higgs-sample/continuous/num-iterations-300/slack-2/offline-step-1.0/online-step-1.0/2017-03-22-13-55/error-rates.txt', header = FALSE, col.names = 'continuous02')
continuous04 = read.csv('higgs-sample/continuous/num-iterations-300/slack-4/offline-step-1.0/online-step-1.0/2017-03-22-14-05/error-rates.txt', header = FALSE, col.names = 'continuous04')
continuous05 = read.csv('higgs-sample/continuous/num-iterations-300/slack-5/offline-step-1.0/online-step-1.0/2017-03-22-14-12/error-rates.txt', header = FALSE, col.names = 'continuous05')
continuous10 = read.csv('higgs-sample/continuous/num-iterations-300/slack-10/offline-step-1.0/online-step-1.0/2017-03-22-10-58/error-rates.txt', header = FALSE, col.names = 'continuous10')

m = max(
        nrow(continuous02), 
      
        nrow(continuous04), 
       
        nrow(continuous05), 
        
        nrow(continuous10))

continuous02 = rbind(continuous02, data.frame(continuous02 = rep(NA, m - nrow(continuous02))))

continuous04 = rbind(continuous04, data.frame(continuous04 = rep(NA, m - nrow(continuous04))))

continuous06 = rbind(continuous05, data.frame(continuous05 = rep(NA, m - nrow(continuous05))))

continuous10 = rbind(continuous10, data.frame(continuous10 = rep(NA, m - nrow(continuous10))))


df = data.frame(time = 1:nrow(continuous02),
                continuous02 = continuous02,
                continuous04 = continuous04,
                continuous05 = continuous05,
                continuous10 = continuous10)

ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error") 



