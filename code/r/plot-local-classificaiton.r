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
continuous = rbind(continuous, data.frame(continuous = rep(tail(continuous[[1]], 1), m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(tail(velox[[1]], 1), m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(tail(baseline[[1]], 1), m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(tail(baselinePlus[[1]], 1), m - nrow(baselinePlus))))

df = data.frame(time = 1:nrow(continuous),
                continuous = continuous, 
                velox = velox, 
                baseline = baseline,
                baselinePlus = baselinePlus)

retrainings = c(32,65,97)

# data frame
p = ggplot(data = df) + 
  # plot lines
  geom_line(aes(x = time, y  = baseline, colour = "a"), size = 1.5) + 
  geom_line(aes(x = time, y  = baselinePlus, colour = "b"), size = 1.5) + 
  geom_line(aes(x = time, y  = continuous, colour = "c"), size = 1.5) + 
  geom_line(aes(x = time, y  = velox, colour = "d"), size = 1.5) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], 
             aes(x=time, y = velox, colour="e"), 
             shape = 17, 
             lwd = 5 ) + 
  # x and y labels
  xlab("Testing Increment") + ylab("Mean Squared Error") + 
  #ylim(c(0.1,1.5)) + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 28), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.key.size  = unit(1.0, "cm"),
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text.y=element_text(size=28),
        axis.text.x=element_text(size=28 ),
        axis.title=element_text(size=28),  
        legend.position=c(0.85,0.70)) + 
  scale_color_manual(name ="",  # Name,
                     labels = c("baseline", "baseline+", "continuous", "velox", "retraining"), 
                     values = c("a" = "green", "b" = "orange", "c" = "blue","d" = "red", "e" = "black"))  + 
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,17),linetype=c(1,1,1,1,0)))) 

ggsave(p , filename = 'cover-types/cover-types-quality.eps', 
       device = 'eps', 
       width = 10, height = 7, 
       units = "in")



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


