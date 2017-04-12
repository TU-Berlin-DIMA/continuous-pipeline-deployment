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

concat <- function(base, filename){
  return(paste(base, sep='/', filename))
}
######################### Directories ########################
COVER_CONTINUOUS = 'cover-types/continuous/num-iterations-500/slack-10/offline-step-1.0/online-step-0.1/2017-04-11-11-25'
COVER_VELOX = 'cover-types/velox/num-iterations-500/slack-62/offline-step-1.0/online-step-0.1/2017-04-12-10-55'
COVER_BASELINE_PLUS = 'cover-types/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-04-11-12-06'
COVER_BASELINE = 'cover-types/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-04-11-13-10'

SEA_CONTINUOUS = 'sea/continuous/num-iterations-500/slack-10/offline-step-1.0/online-step-0.05/2017-04-12-10-35'
SEA_VELOX = 'sea/velox/num-iterations-500/slack-64/offline-step-1.0/online-step-0.05/2017-04-12-10-07'
SEA_BASELINE_PLUS = 'sea/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.05/2017-04-11-23-10'
SEA_BASELINE = 'sea/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-0.05/2017-04-11-23-20'

ADULT_CONTINUOUS = 'adult/continuous/num-iterations-500/slack-10/offline-step-1.0/online-step-0.1/2017-04-11-17-19/'
ADULT_VELOX = 'adult/velox/num-iterations-500/slack-64/offline-step-1.0/online-step-0.1/2017-04-11-17-50'
ADULT_BASELINE_PLUS = 'adult/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-04-11-17-35'
ADULT_BASELINE = 'adult/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-04-11-18-08'

###################### Quality Over Time #####################

#### Cover Types ####
continuous = read.csv(concat(COVER_CONTINUOUS, 'error-rates.txt'), header = FALSE, col.names = 'continuous')
velox = read.csv(concat(COVER_VELOX, 'error-rates.txt'), header = FALSE, col.names = 'velox')
baselinePlus = read.csv(concat(COVER_BASELINE_PLUS, 'error-rates.txt'), header = FALSE, col.names = 'baselinePlus')
baseline= read.csv(concat(COVER_BASELINE, 'error-rates.txt'), header = FALSE, col.names = 'baseline')

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

retrainings = read.csv(concat(COVER_VELOX, 'retraining-points.txt'),header = FALSE)[[1]]

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
  ylim(c(0.24,0.28)) + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=30, color= "black"),  
        legend.position=c(0.12,0.25), 
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
continuous = read.csv(concat(ADULT_CONTINUOUS, 'error-rates.txt'), header = FALSE, col.names = 'continuous')
velox = read.csv(concat(ADULT_VELOX, 'error-rates.txt'), header = FALSE, col.names = 'velox')
baselinePlus = read.csv(concat(ADULT_BASELINE_PLUS, 'error-rates.txt'), header = FALSE, col.names = 'baselinePlus')
baseline= read.csv(concat(ADULT_BASELINE, 'error-rates.txt'), header = FALSE, col.names = 'baseline')

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

retrainings = read.csv(concat(ADULT_VELOX, 'retraining-points.txt'),header = FALSE)[[1]]

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
continuous = read.csv(concat(SEA_CONTINUOUS, 'error-rates.txt'), header = FALSE, col.names = 'continuous')
velox = read.csv(concat(SEA_VELOX, 'error-rates.txt'),  header = FALSE, col.names = 'velox')
baselinePlus = read.csv(concat(SEA_BASELINE_PLUS, 'error-rates.txt'), header = FALSE, col.names = 'baselinePlus')
baseline= read.csv(concat(SEA_BASELINE, 'error-rates.txt'), header = FALSE, col.names = 'baseline')

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

retrainings = read.csv(concat(SEA_VELOX, 'retraining-points.txt'),header = FALSE)[[1]]

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

###################### TOTAL TRAINING TIMES #####################

#### COVER TYPES ####
continuous = read.csv(concat(COVER_CONTINUOUS, 'training-times.txt'), header = FALSE, col.names = 'continuous')
velox = read.csv(concat(COVER_VELOX, 'training-times.txt'), header = FALSE, col.names = 'velox')
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
  scale_y_continuous(expand = c(0, 0), limits = c(0, 750)) +
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
continuous = read.csv(concat(ADULT_CONTINUOUS, 'training-times.txt'), header = FALSE, col.names = 'continuous')
velox = read.csv(concat(ADULT_VELOX, 'training-times.txt'), header = FALSE, col.names = 'velox')
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
  scale_y_continuous(expand = c(0, 0), limits = c(0, 450)) +
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
continuous = read.csv(concat(SEA_CONTINUOUS, 'training-times.txt'), header = FALSE, col.names = 'continuous')
velox = read.csv(concat(SEA_VELOX, 'training-times.txt'),  header = FALSE, col.names = 'velox')
baseline = continuous[[1]][1]
continuous = sum(continuous)
velox = sum(velox)
methods = c('Baseline', 'Continuous', 'Velox')
df = data.frame(methods = methods, time = c(baseline, continuous, velox)/1000)
melted = melt(df, id.vars = 'methods', variable.names = "methods")
melted$methods = factor(as.character(melted$methods), 
                        levels=c("Baseline","Continuous","Velox"))

seaTime = 
  ggplot(melted, aes(x = methods, y = value)) +
  geom_bar(stat='identity') + 
  xlab("") + ylab("Time (s)") + 
 scale_y_continuous(expand = c(0, 0), limits = c(0, 1000)) +
  theme_bw() + 
  theme(legend.position="none",
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color = "black"),  
        plot.margin = unit(c(0.4, 0.0, -0.80, 0.2), "cm"))

ggsave(seaTime , filename = 'sea/sea-times.eps', 
       device = 'eps',
       width = 7, height = 5, 
       units = "in")


###################### META: PERFORMANCE VS QUALITY #####################

#### COVER TYPES ####
continuousTime = read.csv(concat(COVER_CONTINUOUS, 'training-times.txt'), header = FALSE, col.names = 'continuous')
veloxTime = read.csv(concat(COVER_VELOX, 'training-times.txt'), header = FALSE, col.names = 'velox')
baselineTime = continuousTime[[1]][1]

continuous = read.csv(concat(COVER_CONTINUOUS, 'error-rates.txt'), header = FALSE, col.names = 'continuous')
velox = read.csv(concat(COVER_VELOX, 'error-rates.txt'), header = FALSE, col.names = 'velox')
baseline = read.csv(concat(COVER_BASELINE,'/error-rates.txt'), header = FALSE)


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

#### ADULT ####
continuousTime = read.csv(concat(ADULT_CONTINUOUS, 'training-times.txt'), header = FALSE, col.names = 'continuous')
veloxTime = read.csv(concat(ADULT_VELOX, 'training-times.txt'), header = FALSE, col.names = 'continuous')
baselineTime = continuousTime[[1]][1]

continuous =read.csv(concat(ADULT_CONTINUOUS, 'error-rates.txt'), header = FALSE)
velox = read.csv(concat(ADULT_VELOX, 'error-rates.txt'), header = FALSE)
baseline = read.csv(concat(ADULT_BASELINE, 'error-rates.txt'), header = FALSE)


df = data.frame('error'=c(colMeans(continuous), colMeans(velox), colMeans(baseline)), 
                'time' = c(sum(continuousTime)/1000, sum(veloxTime)/1000, baselineTime/1000), 
                'models'=c('Continuous', 'Velox', 'Baseline'))
p = 
  ggplot(data = df, aes(x = time, y = error)) + 
  geom_point(aes(shape = models),  lwd = 12) + 
  #geom_text(aes(label = models, colour = models), size = 5, fontface ="bold", hjust="inward", vjust="inward", show.legend  = F, angle = 45)  + 
  xlab("Time (s)") + ylab("Avg Error rate") + 
  ylim(c(0.1580, 0.163)) + 
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
continuousTime = read.csv(concat(SEA_CONTINUOUS, 'training-times.txt'), header = FALSE, col.names = 'continuous')
veloxTime = read.csv(concat(SEA_VELOX, training-times.txt),  header = FALSE, col.names = 'velox')
baselineTime = continuousTime[[1]][1]

continuous = read.csv(concat(SEA_CONTINUOUS, 'error-rates.txt'), header = FALSE, col.names = 'continuous')
velox = read.csv(concat(SEA_VELOX, 'error-rates.txt'), header = FALSE, col.names = 'velox')
baseline = read.csv(concat(SEA_BASELINE, 'error-rates.txt'), header = FALSE)


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