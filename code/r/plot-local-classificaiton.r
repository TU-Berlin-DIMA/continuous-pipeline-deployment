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
continuous = read.csv('cover-types/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-22-33/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('cover-types/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-22-29/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('cover-types/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-22-37/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('cover-types/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-22-39/error-rates.txt', header = FALSE, col.names = 'baseline')

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

retrainings = c(32,63,96)

# data frame
p = 
  ggplot(data = df) + 
  # plot lines
  geom_line(aes(x = time, y  = baseline, linetype = "a", color = "a"), size = 1, linetype = "dotted") + 
  geom_line(aes(x = time, y  = baselinePlus, linetype = "b", color = "b"), linetype = "dotdash", size = 1) + 
  geom_line(aes(x = time, y  = continuous, linetype = "c", color = "c"), linetype = "solid", size = 1) + 
  geom_line(aes(x = time, y  = velox, linetype = "d", color = "d"), linetype = "longdash", size = 1) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], aes(x=time, y = velox, shape = "e", color = "e"), lwd = 4, shape = 17 ) + 
  # x and y labels
  xlab("Time") + ylab("Misclassification Rate") + 
  #ylim(c(0.1,1.5)) + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 20, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color= "black"),  
        legend.position=c(0.85,0.67), 
        legend.key.width = unit(2.5, "cm"), 
        legend.key.height = unit(0.8, "cm")) + 
  scale_linetype_discrete(guide=FALSE) + 
  scale_shape_discrete(guide=FALSE) + 
  scale_color_manual(name = "", 
                     labels = c("baseline", "baseline+", "continuous","velox", "retraining"),
                     values = c("a"="black", "b"="black","c"="black","d"="black", "e"="black"))+
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,17),linetype=c(3,4,1,5,0)))) 
  
ggsave(p , filename = 'cover-types/cover-types-quality.eps', 
       device = 'eps', 
       width = 14, height = 5, 
       units = "in")

# higgs sample
continuous = read.csv('higgs-sample/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-23-48/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('higgs-sample/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-23-08/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('higgs-sample/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-23-51/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('higgs-sample/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-23-55/error-rates.txt', header = FALSE, col.names = 'baseline')

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

retrainings = c(32,66,98)

# data frame
p = 
  ggplot(data = df) + 
  # plot lines
  geom_line(aes(x = time, y  = baseline, linetype = "a", color = "a"), size = 1, linetype = "dotted") + 
  geom_line(aes(x = time, y  = baselinePlus, linetype = "b", color = "b"), linetype = "dotdash", size = 1) + 
  geom_line(aes(x = time, y  = continuous, linetype = "c", color = "c"), linetype = "solid", size = 1) + 
  geom_line(aes(x = time, y  = velox, linetype = "d", color = "d"), linetype = "longdash", size = 1) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], aes(x=time, y = velox, shape = "e", color = "e"), lwd = 4, shape = 17 ) + 
  # x and y labels
  xlab("Time") + ylab("Misclassification Rate") + 
  #ylim(c(0.1,1.5)) + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 20, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color= "black"),  
        legend.position=c(0.25,0.30), 
        legend.key.width = unit(2.5, "cm"), 
        legend.key.height = unit(0.8, "cm")) + 
  scale_linetype_discrete(guide=FALSE) + 
  scale_shape_discrete(guide=FALSE) + 
  scale_color_manual(name = "", 
                     labels = c("baseline", "baseline+", "continuous","velox", "retraining"),
                     values = c("a"="black", "b"="black","c"="black","d"="black", "e"="black"))+
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,17),linetype=c(3,4,1,5,0)))) 


ggsave(p , filename = 'higgs-sample/higgs-sample-quality.eps', 
       device = 'eps', 
       width = 14, height = 5, 
       units = "in")


# Susy Sample
continuous = read.csv('susy-sample/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-23-23/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('susy-sample/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-22-55/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('susy-sample/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-23-37/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('susy-sample/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-23-42/error-rates.txt', header = FALSE, col.names = 'baseline')

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

retrainings = c(32,66,98)

# data frame
p =
  ggplot(data = df) + 
  # plot lines
  geom_line(aes(x = time, y  = baseline, linetype = "a", color = "a"), size = 1, linetype = "dotted") + 
  geom_line(aes(x = time, y  = baselinePlus, linetype = "b", color = "b"), linetype = "dotdash", size = 1) + 
  geom_line(aes(x = time, y  = continuous, linetype = "c", color = "c"), linetype = "solid", size = 1) + 
  geom_line(aes(x = time, y  = velox, linetype = "d", color = "d"), linetype = "longdash", size = 1) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], aes(x=time, y = velox, shape = "e", color = "e"), lwd = 4, shape = 17 ) + 
  # x and y labels
  xlab("Time") + ylab("Misclassification Rate") + 
  #ylim(c(0.1,1.5)) + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 20, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color= "black"),  
        legend.position=c(0.85,0.67), 
        legend.key.width = unit(2.5, "cm"), 
        legend.key.height = unit(0.8, "cm")) + 
  scale_linetype_discrete(guide=FALSE) + 
  scale_shape_discrete(guide=FALSE) + 
  scale_color_manual(name = "", 
                     labels = c("baseline", "baseline+", "continuous","velox", "retraining"),
                     values = c("a"="black", "b"="black","c"="black","d"="black", "e"="black"))+
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,17),linetype=c(3,4,1,5,0)))) 
ggsave(p , filename = 'susy-sample/susy-sample-quality.eps', 
       device = 'eps', 
       width = 14, height = 5, 
       units = "in")


# Plot Training times

## Cover Type
continuous = read.csv('cover-types/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-22-33/training-times.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('cover-types/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-22-29/training-times.txt', header = FALSE, col.names = 'velox')
baseline = continuous[[1]][1]
continuous = sum(continuous)
velox = sum(velox)
methods = c('Baseline', 'Continuous', 'Velox')
df = data.frame(methods = methods, time = c(baseline, continuous, velox)/1000)
melted = melt(df, id.vars = 'methods', variable.names = "methods")
melted$methods = factor(as.character(melted$methods), 
                    levels=c("Baseline","Continuous","Velox"))

coverTypeTime = 
  ggplot(melted, aes(x = methods, y = value)) +
  geom_bar(stat='identity') + 
  xlab("") + ylab("Time (s)") + 
  scale_y_continuous(expand = c(0, 0), limits = c(0, 150)) +
  theme_bw() + 
  theme(legend.position="none",
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color = "black"),  
        plot.margin = unit(c(0.4, 0.0, -0.80, 0.2), "cm"))

ggsave(coverTypeTime , filename = 'cover-types/cover-types-times.eps', 
       device = 'eps',
       width = 7, height = 5, 
       units = "in")


## SUSY SAMPLE
continuous = read.csv('susy-sample/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-23-23/training-times.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('susy-sample/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-22-55/training-times.txt', header = FALSE, col.names = 'velox')
baseline = continuous[[1]][1]
continuous = sum(continuous)
velox = sum(velox)
methods = c('Baseline', 'Continuous', 'Velox')
df = data.frame(methods = methods, time = c(baseline, continuous, velox)/1000)
melted = melt(df, id.vars = 'methods', variable.names = "methods")
melted$methods = factor(as.character(melted$methods), 
                        levels=c("Baseline","Continuous","Velox"))

susyTime = 
  ggplot(melted, aes(x = methods, y = value)) +
  geom_bar(stat='identity') + 
  xlab("") + ylab("Time (s)") + 
  scale_y_continuous(expand = c(0, 0), limits = c(0, 150)) +
  theme_bw() + 
  theme(legend.position="none",
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color = "black"),  
        plot.margin = unit(c(0.4, 0.0, -0.80, 0.2), "cm"))

ggsave(susyTime , filename = 'susy-sample/susy-sample-times.eps', 
       device = 'eps',
       width = 7, height = 5, 
       units = "in")

## HIGGS SAMPLE
continuous = read.csv('higgs-sample/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-23-48/training-times.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('higgs-sample/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-23-08/training-times.txt', header = FALSE, col.names = 'velox')
baseline = continuous[[1]][1]
continuous = sum(continuous)
velox = sum(velox)
methods = c('Baseline', 'Continuous', 'Velox')
df = data.frame(methods = methods, time = c(baseline, continuous, velox)/1000)
melted = melt(df, id.vars = 'methods', variable.names = "methods")
melted$methods = factor(as.character(melted$methods), 
                        levels=c("Baseline","Continuous","Velox"))


higgsTime = 
  ggplot(melted, aes(x = methods, y = value)) +
  geom_bar(stat='identity') + 
  xlab("") + ylab("Time (s)") + 
  scale_y_continuous(expand = c(0, 0), limits = c(0, 150)) +
  theme_bw() + 
  theme(legend.position="none",
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color = "black"),  
        plot.margin = unit(c(0.4, 0.0, -0.80, 0.2), "cm"))

ggsave(higgsTime , filename = 'higgs-sample/higgs-sample-times.eps', 
       device = 'eps',
       width = 7, height = 5, 
       units = "in")
