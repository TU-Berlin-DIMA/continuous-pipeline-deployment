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

###################### Quality Over Time #####################

#### Cover Types ####
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
  geom_line(aes(x = time, y  = baseline, linetype = "a", color = "a"), size = 1.4, linetype = "dotted") + 
  geom_line(aes(x = time, y  = baselinePlus, linetype = "b", color = "b"), linetype = "dotdash", size = 1.4) + 
  geom_line(aes(x = time, y  = continuous, linetype = "c", color = "c"), linetype = "solid", size = 1.4) + 
  geom_line(aes(x = time, y  = velox, linetype = "d", color = "d"), linetype = "longdash", size = 1.4) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], aes(x=time, y = velox, shape = "e", color = "e"), lwd = 8, shape = 18 ) + 
  # x and y labels
  xlab("Time") + ylab("Error Rate") + 
  ylim(c(0.24,0.27)) + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=30, color= "black"),  
        legend.position=c(0.15,0.25), 
        legend.key.width = unit(3, "cm"), 
        legend.key.height = unit(0.8, "cm")) + 
  scale_linetype_discrete(guide=FALSE) + 
  scale_shape_discrete(guide=FALSE) + 
  scale_color_manual(name = "", 
                     labels = c("baseline", "baseline+", "continuous","velox", "retraining"),
                     values = c("a"="black", "b"="black","c"="black","d"="black", "e"="black"))+
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,18),linetype=c(3,4,1,5,0)))) 
  
ggsave(p , filename = 'cover-types/cover-types-quality.eps', 
       device = 'eps', 
       width = 14, height = 5, 
       units = "in")

#### ADULT ####
continuous = read.csv('adult/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-0.3/2017-03-29-17-58/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('adult/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-0.3/2017-03-29-17-23/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('adult/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.3/2017-03-29-17-18/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline = read.csv('adult/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-03-29-15-08/error-rates.txt', header = FALSE, col.names = 'baseline')


m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))

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
  geom_line(aes(x = time, y  = baseline, linetype = "a", color = "a"), size = 1.4, linetype = "dotted") + 
  geom_line(aes(x = time, y  = baselinePlus, linetype = "b", color = "b"), linetype = "dotdash", size = 1.4) + 
  geom_line(aes(x = time, y  = continuous, linetype = "c", color = "c"), linetype = "solid", size = 1.4) + 
  geom_line(aes(x = time, y  = velox, linetype = "d", color = "d"), linetype = "longdash", size = 1.4) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], aes(x=time, y = velox, shape = "e", color = "e"), lwd = 8, shape = 18 ) + 
  # x and y labels
  xlab("Time") + ylab("Error Rate") + 
  #ylim(c(0.1,1.5)) + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=30, color= "black"),  
        legend.position=c(0.85,0.30), 
        legend.key.width = unit(3, "cm"), 
        legend.key.height = unit(0.8, "cm")) + 
  scale_linetype_discrete(guide=FALSE) + 
  scale_shape_discrete(guide=FALSE) + 
  scale_color_manual(name = "", 
                     labels = c("baseline", "baseline+", "continuous","velox", "retraining"),
                     values = c("a"="black", "b"="black","c"="black","d"="black", "e"="black"))+
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,18),linetype=c(3,4,1,5,0)))) 

ggsave(p , filename = 'adult/adult-quality.eps', 
       device = 'eps', 
       width = 14, height = 5, 
       units = "in")



#### SEA ####
continuous = read.csv('sea/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-0.05/2017-03-29-10-40/error-rates.txt', header = FALSE, col.names = 'continuous')

velox = read.csv('sea/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-0.05/2017-03-29-10-43/error-rates.txt', header = FALSE, col.names = 'velox')

baselinePlus = read.csv('sea/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.05/2017-03-29-10-47/error-rates.txt', header = FALSE, col.names = 'baselinePlus')

baseline= read.csv('sea/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-03-29-10-29/error-rates.txt', header = FALSE, col.names = 'baseline')

m = max(nrow(continuous), nrow(velox), nrow(baseline), nrow(baselinePlus))
continuous = rbind(continuous, data.frame(continuous = rep(NA, m - nrow(continuous))))
velox = rbind(velox, data.frame(velox = rep(NA, m - nrow(velox))))
baseline = rbind(baseline, data.frame(baseline = rep(NA, m - nrow(baseline))))
baselinePlus = rbind(baselinePlus, data.frame(baselinePlus = rep(NA, m - nrow(baselinePlus))))

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
  geom_line(aes(x = time, y  = baseline, linetype = "a", color = "a"), size = 1.4, linetype = "dotted") + 
  geom_line(aes(x = time, y  = baselinePlus, linetype = "b", color = "b"), linetype = "dotdash", size = 1.4) + 
  geom_line(aes(x = time, y  = continuous, linetype = "c", color = "c"), linetype = "solid", size = 1.4) + 
  geom_line(aes(x = time, y  = velox, linetype = "d", color = "d"), linetype = "longdash", size = 1.4) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], aes(x=time, y = velox, shape = "e", color = "e"), lwd = 8, shape = 18 ) + 
  # x and y labels
  xlab("Time") + ylab("Error Rate") + 
  #ylim(c(0.1,1.5)) + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=30, color= "black"),  
        legend.position=c(0.70,0.80), 
        legend.key.width = unit(3, "cm"), 
        legend.key.height = unit(0.8, "cm")) + 
  scale_linetype_discrete(guide=FALSE) + 
  scale_shape_discrete(guide=FALSE) + 
  scale_color_manual(name = "", 
                     labels = c("baseline", "baseline+", "continuous","velox", "retraining"),
                     values = c("a"="black", "b"="black","c"="black","d"="black", "e"="black"))+
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,18),linetype=c(3,4,1,5,0)))) 

ggsave(p , filename = 'sea/sea-quality.eps', 
       device = 'eps', 
       width = 14, height = 5, 
       units = "in")


#### HIGGS SAMPLE ####
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
  geom_line(aes(x = time, y  = baseline, linetype = "a", color = "a"), size = 1.4, linetype = "dotted") + 
  geom_line(aes(x = time, y  = baselinePlus, linetype = "b", color = "b"), linetype = "dotdash", size = 1.4) + 
  geom_line(aes(x = time, y  = continuous, linetype = "c", color = "c"), linetype = "solid", size = 1.4) + 
  geom_line(aes(x = time, y  = velox, linetype = "d", color = "d"), linetype = "longdash", size = 1.4) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], aes(x=time, y = velox, shape = "e", color = "e"), lwd = 8, shape = 18 ) + 
  # x and y labels
  xlab("Time") + ylab("Error Rate") + 
  #ylim(c(0.1,1.5)) + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=30, color= "black"),  
        legend.position=c(0.25,0.30), 
        legend.key.width = unit(3, "cm"), 
        legend.key.height = unit(0.8, "cm")) + 
  scale_linetype_discrete(guide=FALSE) + 
  scale_shape_discrete(guide=FALSE) + 
  scale_color_manual(name = "", 
                     labels = c("baseline", "baseline+", "continuous","velox", "retraining"),
                     values = c("a"="black", "b"="black","c"="black","d"="black", "e"="black"))+
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,18),linetype=c(3,4,1,5,0)))) 


ggsave(p , filename = 'higgs-sample/higgs-sample-quality.eps', 
       device = 'eps', 
       width = 14, height = 5, 
       units = "in")


#### SUSY SAMPLE ####
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
  geom_line(aes(x = time, y  = baseline, linetype = "a", color = "a"), size = 1.4, linetype = "dotted") + 
  geom_line(aes(x = time, y  = baselinePlus, linetype = "b", color = "b"), linetype = "dotdash", size = 1.4) + 
  geom_line(aes(x = time, y  = continuous, linetype = "c", color = "c"), linetype = "solid", size = 1.4) + 
  geom_line(aes(x = time, y  = velox, linetype = "d", color = "d"), linetype = "longdash", size = 1.4) + 
  # plot retraining points
  geom_point(data = df[retrainings,c(1,3)], aes(x=time, y = velox, shape = "e", color = "e"), lwd = 8, shape = 18 ) + 
  # x and y labels
  xlab("Time") + ylab("Error Rate") + 
  #ylim(c(0.1,1.5)) + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=30, color= "black"),  
        legend.position=c(0.85,0.67), 
        legend.key.width = unit(3, "cm"), 
        legend.key.height = unit(0.8, "cm")) + 
  scale_linetype_discrete(guide=FALSE) + 
  scale_shape_discrete(guide=FALSE) + 
  scale_color_manual(name = "", 
                     labels = c("baseline", "baseline+", "continuous","velox", "retraining"),
                     values = c("a"="black", "b"="black","c"="black","d"="black", "e"="black"))+
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,18),linetype=c(3,4,1,5,0)))) 

ggsave(p , filename = 'susy-sample/susy-sample-quality.eps', 
       device = 'eps', 
       width = 14, height = 5, 
       units = "in")


###################### TOTAL TRAINING TIMES #####################

#### COVER TYPES ####
continuous = read.csv('cover-types/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-29-18-03/training-times.txt', header = FALSE, col.names = 'continuous')
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
  scale_y_continuous(expand = c(0, 0), limits = c(0, 175)) +
  theme_bw() + 
  theme(legend.position="none",
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color = "black"),  
        plot.margin = unit(c(0.4, 0.0, -0.80, 0.2), "cm"))

ggsave(coverTypeTime , filename = 'cover-types/cover-types-times.eps', 
       device = 'eps',
       width = 7, height = 5, 
       units = "in")

#### ADULT ####
continuous = read.csv('adult/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-0.3/2017-03-29-17-58/training-times.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('adult/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-0.3/2017-03-29-17-23/training-times.txt', header = FALSE, col.names = 'velox')
baseline = continuous[[1]][1]
continuous = sum(continuous)
velox = sum(velox)
methods = c('Baseline', 'Continuous', 'Velox')
df = data.frame(methods = methods, time = c(baseline, continuous, velox)/1000)
melted = melt(df, id.vars = 'methods', variable.names = "methods")
melted$methods = factor(as.character(melted$methods), 
                        levels=c("Baseline","Continuous","Velox"))

adultTime = 
  ggplot(melted, aes(x = methods, y = value)) +
  geom_bar(stat='identity') + 
  xlab("") + ylab("Time (s)") + 
  scale_y_continuous(expand = c(0, 0), limits = c(0, 175)) +
  theme_bw() + 
  theme(legend.position="none",
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color = "black"),  
        plot.margin = unit(c(0.4, 0.0, -0.80, 0.2), "cm"))

ggsave(adultTime , filename = 'adult/adult-times.eps', 
       device = 'eps',
       width = 7, height = 5, 
       units = "in")
#### SEA ####
continuous = read.csv('sea/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-0.05/2017-03-29-10-40/training-times.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('sea/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-0.05/2017-03-29-10-43/training-times.txt', header = FALSE, col.names = 'velox')
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
  scale_y_continuous(expand = c(0, 0), limits = c(0, 175)) +
  theme_bw() + 
  theme(legend.position="none",
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color = "black"),  
        plot.margin = unit(c(0.4, 0.0, -0.80, 0.2), "cm"))

ggsave(susyTime , filename = 'sea/sea-times.eps', 
       device = 'eps',
       width = 7, height = 5, 
       units = "in")



#### SUSY SAMPLE ####
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

#### HIGGS SAMPLE ####
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




###################### META: PERFORMANCE VS QUALITY #####################

#### COVER TYPES ####
continuousTime = read.csv('cover-types/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-22-33/training-times.txt', header = FALSE)
veloxTime = read.csv('cover-types/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-22-29/training-times.txt', header = FALSE)
baselineTime = continuousTime[[1]][1]

continuous = read.csv('cover-types/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-22-33/error-rates.txt', header = FALSE)
velox = read.csv('cover-types/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-22-29/error-rates.txt', header = FALSE)
baseline = read.csv('cover-types/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-22-39/error-rates.txt', header = FALSE)


df = data.frame('error'=c(colMeans(continuous), colMeans(velox), colMeans(baseline)), 
                'time' = c(sum(continuousTime)/1000, sum(veloxTime)/1000, baselineTime/1000), 
                'models'=c('Continuous', 'Velox', 'Baseline'))
p = 
  ggplot(data = df, aes(x = time, y = error)) + 
  geom_point(aes(shape = models),  lwd = 12) + 
  #geom_text(aes(label = models, colour = models), size = 5, fontface ="bold", hjust="inward", vjust="inward", show.legend  = F, angle = 45)  + 
  xlab("Time (s)") + ylab("Avg Error rate") + 
  ylim(c(0.24, 0.30)) + 
  theme_bw() + 
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.title = element_text(size = 30, color = "black"),
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"),
        legend.key.width  = unit(1.0, "cm"), 
        legend.key.height  = unit(1.0, "cm"), 
        legend.position=c(0.75,0.85)) +
  theme(axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=32, color = "black")) + 
  scale_shape_manual("", values = c("Baseline" = 15, "Continuous" = 16, "Velox" = 18))

ggsave(p , filename = 'cover-types/cover-types-meta-performance.eps', 
       device = cairo_ps,
       width = 7, height = 5, 
       units = "in")

#### HIGGS SAMPLE ####
continuousTime = read.csv('higgs-sample/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-23-48/training-times.txt', header = FALSE)
veloxTime = read.csv('higgs-sample/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-23-08/training-times.txt', header = FALSE)
baselineTime = continuousTime[[1]][1]

continuous = read.csv('higgs-sample/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-23-48/error-rates.txt', header = FALSE)
velox = read.csv('higgs-sample/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-23-08/error-rates.txt', header = FALSE)
baseline = read.csv('higgs-sample/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-23-55/error-rates.txt', header = FALSE)


df = data.frame('error'=c(colMeans(continuous), colMeans(velox), colMeans(baseline)), 
                'time' = c(sum(continuousTime)/1000, sum(veloxTime)/1000, baselineTime/1000), 
                'models'=c('Continuous', 'Velox', 'Baseline'))
p = 
  ggplot(data = df, aes(x = time, y = error)) + 
  geom_point(aes(shape = models),  lwd = 12) + 
  #geom_text(aes(label = models, colour = models), size = 5, fontface ="bold", hjust="inward", vjust="inward", show.legend  = F, angle = 45)  + 
  xlab("Time (s)") + ylab("Avg Error rate") + 
  ylim(c(0.372, 0.379)) + 
  theme_bw() + 
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.title = element_text(size = 30, color = "black"),
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"),
        legend.key.width  = unit(1.0, "cm"), 
        legend.key.height  = unit(1.0, "cm"), 
        legend.position=c(0.75,0.85)) +
  theme(axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=32, color = "black")) + 
  scale_shape_manual("", values = c("Baseline" = 15, "Continuous" = 16, "Velox" = 18))

ggsave(p , filename = 'higgs-sample/higgs-sample-meta-performance.eps', 
       device = cairo_ps,
       width = 7, height = 5, 
       units = "in")

#### SUSY SAMPLE ####
continuousTime = read.csv('susy-sample/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-23-23/training-times.txt', header = FALSE)
veloxTime = read.csv('susy-sample/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-22-55/training-times.txt', header = FALSE)
baselineTime = continuousTime[[1]][1]

continuous = read.csv('susy-sample/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-1.0/2017-03-23-23-23/error-rates.txt', header = FALSE)
velox = read.csv('susy-sample/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-1.0/2017-03-23-22-55/error-rates.txt', header = FALSE)
baseline = read.csv('susy-sample/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-03-23-23-42/error-rates.txt', header = FALSE)


df = data.frame('error'=c(colMeans(continuous), colMeans(velox), colMeans(baseline)), 
                'time' = c(sum(continuousTime)/1000, sum(veloxTime)/1000, baselineTime/1000), 
                'models'=c('Continuous', 'Velox', 'Baseline'))
p = 
  ggplot(data = df, aes(x = time, y = error)) + 
  geom_point(aes(shape = models),  lwd = 12) + 
  #geom_text(aes(label = models, colour = models), size = 5, fontface ="bold", hjust="inward", vjust="inward", show.legend  = F, angle = 45)  + 
  xlab("Time (s)") + ylab("Avg Error rate") + 
  ylim(c(0.223, 0.227)) + 
  theme_bw() + 
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.title = element_text(size = 30, color = "black"),
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"),
        legend.key.width  = unit(1.0, "cm"), 
        legend.key.height  = unit(1.0, "cm"), 
        legend.position=c(0.75,0.85)) +
  theme(axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=32, color = "black")) + 
  scale_shape_manual("", values = c("Baseline" = 15, "Continuous" = 16, "Velox" = 18))

ggsave(p , filename = 'susy-sample/susy-sample-meta-performance.eps', 
       device = cairo_ps,
       width = 7, height = 5, 
       units = "in")

#### ADULT ####
continuousTime = read.csv('adult/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-0.3/2017-03-29-17-58/training-times.txt', header = FALSE)
veloxTime = read.csv('adult/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-0.3/2017-03-29-17-23/training-times.txt', header = FALSE)
baselineTime = continuousTime[[1]][1]

continuous = read.csv('adult/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-0.3/2017-03-29-17-58/error-rates.txt', header = FALSE)
velox = read.csv('adult/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-0.3/2017-03-29-17-23/error-rates.txt', header = FALSE)
baseline = read.csv('adult/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-03-29-15-08/error-rates.txt', header = FALSE)


df = data.frame('error'=c(colMeans(continuous), colMeans(velox), colMeans(baseline)), 
                'time' = c(sum(continuousTime)/1000, sum(veloxTime)/1000, baselineTime/1000), 
                'models'=c('Continuous', 'Velox', 'Baseline'))
p = 
  ggplot(data = df, aes(x = time, y = error)) + 
  geom_point(aes(shape = models),  lwd = 12) + 
  #geom_text(aes(label = models, colour = models), size = 5, fontface ="bold", hjust="inward", vjust="inward", show.legend  = F, angle = 45)  + 
  xlab("Time (s)") + ylab("Avg Error rate") + 
  ylim(c(0.1580, 0.161)) + 
  theme_bw() + 
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.title = element_text(size = 30, color = "black"),
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"),
        legend.key.width  = unit(1.0, "cm"), 
        legend.key.height  = unit(1.0, "cm"), 
        legend.position=c(0.75,0.85)) +
  theme(axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=32, color = "black")) + 
  scale_shape_manual("", values = c("Baseline" = 15, "Continuous" = 16, "Velox" = 18))

ggsave(p , filename = 'adult/adult-meta-performance.eps', 
       device = cairo_ps,
       width = 7, height = 5, 
       units = "in")

#### SEA ####
continuousTime = read.csv('sea/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-0.05/2017-03-29-10-40/training-times.txt', header = FALSE, col.names = 'continuous')
veloxTime = read.csv('sea/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-0.05/2017-03-29-10-43/training-times.txt', header = FALSE, col.names = 'velox')
baselineTime = continuousTime[[1]][1]

continuous = read.csv('sea/continuous/num-iterations-500/slack-5/offline-step-1.0/online-step-0.05/2017-03-29-10-40/error-rates.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('sea/velox/num-iterations-500/slack-32/offline-step-1.0/online-step-0.05/2017-03-29-10-43/error-rates.txt', header = FALSE, col.names = 'velox')
baseline = read.csv('sea/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-03-29-10-29/error-rates.txt', header = FALSE)


df = data.frame('error'=c(colMeans(continuous), colMeans(velox), colMeans(baseline)), 
                'time' = c(sum(continuousTime)/1000, sum(veloxTime)/1000, baselineTime/1000), 
                'models'=c('Continuous', 'Velox', 'Baseline'))
p = 
  ggplot(data = df, aes(x = time, y = error)) + 
  geom_point(aes(shape = models),  lwd = 12) + 
  #geom_text(aes(label = models, colour = models), size = 5, fontface ="bold", hjust="inward", vjust="inward", show.legend  = F, angle = 45)  + 
  xlab("Time (s)") + ylab("Avg Error rate") + 
  ylim(c(0.270, 0.30)) + 
  theme_bw() + 
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.title = element_text(size = 30, color = "black"),
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"),
        legend.key.width  = unit(1.0, "cm"), 
        legend.key.height  = unit(1.0, "cm"), 
        legend.position=c(0.75,0.85)) +
  theme(axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=32, color = "black")) + 
  scale_shape_manual("", values = c("Baseline" = 15, "Continuous" = 16, "Velox" = 18))

ggsave(p , filename = 'sea/sea-meta-performance.eps', 
       device = cairo_ps,
       width = 7, height = 5, 
       units = "in")