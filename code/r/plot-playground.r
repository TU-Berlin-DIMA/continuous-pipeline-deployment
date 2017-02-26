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

# Plot CRITEO Sample Results
continuous = read.csv('criteo-sample/continuous/batch-1-slack-10-incremental-false-error-cumulative/2017-02-23-15-28/error-rates.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('criteo-sample/velox/batch-1-slack-20-incremental-true-error-cumulative/2017-02-23-14-51/error-rates.txt', header = FALSE, col.names = 'velox')
baselinePlus = read.csv('criteo-sample/baseline-plus/batch-1-slack-none-incremental-true-error-cumulative/2017-02-23-15-46/error-rates.txt', header = FALSE, col.names = 'baselinePlus')
baseline= read.csv('criteo-sample/baseline/batch-1-slack-none-incremental-false-error-cumulative/2017-02-23-15-38/error-rates.txt', header = FALSE, col.names = 'baseline')

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous, 
                velox = velox,
                baseline = baseline,
                baselinePlus = baselinePlus)

#retrainings = c(830,1699,2485,3355,4204,5000)

  # data frame
  ggplot(data = df) + 
  # plot lines
  geom_line(aes(x = time, y  = baseline, colour = "a"), size = 1.5) + 
  geom_line(aes(x = time, y  = baselinePlus, colour = "b"), size = 1.5) + 
  geom_line(aes(x = time, y  = continuous, colour = "c"), size = 1.5) + 
  geom_line(aes(x = time, y  = velox, colour = "d"), size = 1.5) + 
  # plot retraining points
  #geom_point(data = df[retrainings,c(1,3)], 
  #           aes(x=time, y = velox, colour="e", fill = "Retraining"), 
  #           shape = 24, 
  #           lwd = 7 ) + 
  # x and y labels
  xlab("Test Cycle") + ylab("Mean Squared Error") + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 26), legend.key = element_rect(colour = "transparent", fill = alpha('white', 0.0)) ,
        legend.position="bottom") +
  theme(axis.text=element_text(size=26),
        axis.title=element_text(size=28)) + 
  # legend for line graph   
  scale_color_manual(name ="",  # Name,
                     labels = c("Baseline   ", "Baseline+    ", "Continuous    ", "Velox    "), 
                     values = c("a" = "green", "b" = "orange", "c" = "blue","d" = "red"))  
# legend for retraining point
#scale_fill_manual(name = "", values = c("Retraining" = "red")) + 
# guides for enhancing legend
#guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,NA),linetype=c(1,1,1,1,0)))) 
  
  
# Plot cover type Results
continuous = read.csv('cover-types/continuous/batch-1-slack-4-incremental-true-error-cumulative-prequential-1.0/2017-02-25-12-45/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('cover-types/velox/batch-1-slack-20-incremental-true-error-cumulative-prequential-1.0/2017-02-25-12-47/error-rates.txt', header = FALSE, col.names = 'velox')

#baselinePlus = read.csv('cover-types/baseline-plus/batch-1-slack-none-incremental-true-error-cumulative-prequential-1.0/2017-02-25-12-30/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

#baseline= read.csv('cover-types/baseline/batch-1-slack-none-incremental-false-error-cumulative-prequential-1.0/2017-02-25-12-28/error-rates.txt', header = FALSE, col.names = 'baseline')

#m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
#continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
#velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
#baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
#baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))
df = data.frame(time = 1:nrow(continuous),
                continuous = continuous, 
                velox = velox)
               # baseline = baseline,
                #baselinePlus = baselinePlus)
ml = melt(df, id.vars = 'time' )
ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
  geom_line(aes( colour = variable)) + 
  xlab("Time") + ylab("Mean Squared Error") 
