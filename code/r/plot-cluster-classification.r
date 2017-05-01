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

######################### Directories ########################
HIGGS_CONTINUOUS = 'higgs/continuous/num-iterations-500/slack-50/offline-step-1.0/online-step-0.1/2017-04-10-15-52'
HIGGS_VELOX = 'higgs/velox/num-iterations-500/slack-320/offline-step-1.0/online-step-0.1/2017-04-10-17-17'
HIGGS_BASELINE_PLUS = 'higgs/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-04-10-18-55'
HIGGS_BASELINE = 'higgs/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-04-11-10-31'

URL_CONTINUOUS = 'url-reputation/continuous/num-iterations-500/slack-200/offline-step-1.0/online-step-0.1/2017-04-24-00-41/error-rates.txt'
URL_VELOX = 'url-reputation/velox/num-iterations-500/slack-1280/offline-step-1.0/online-step-0.1/2017-04-12-02-46/error-rates.txt'
URL_BASELINE_PLUS = 'url-reputation/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-04-24-05-46/error-rates.txt'
URL_BASELINE = 'url-reputation/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-04-24-12-11/error-rates.txt'


###################### Quality Over Time #####################

#### HIGGS ####
continuous = read.csv(paste(HIGGS_CONTINUOUS,'error-rates.txt', sep='/'), header = FALSE, col.names = 'continuous')
velox = read.csv(paste(HIGGS_VELOX,'error-rates.txt', sep='/'), header = FALSE, col.names = 'velox')
baselinePlus = read.csv(paste(HIGGS_BASELINE_PLUS,'error-rates.txt', sep='/'), header = FALSE, col.names = 'baselinePlus')
baseline = read.csv(paste(HIGGS_BASELINE,'error-rates.txt', sep='/'), header = FALSE, col.names = 'baseline')

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

retrainings = seq(300, 3600, 320)

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
  #ylim(c(0.367,0.381)) + 
  # legend themes
  theme_bw() + 
  theme(legend.text = element_text(size = 30, color = "black"), 
        legend.key = element_rect(colour = "transparent", fill = "transparent"), 
        legend.background = element_rect(colour = "transparent", fill = "transparent"), 
        axis.text=element_text(size=30, color = "black"),
        axis.title=element_text(size=30, color= "black"),  
        legend.position=c(0.85,0.7), 
        legend.key.width = unit(3, "cm"), 
        legend.key.height = unit(0.8, "cm")) + 
  scale_linetype_discrete(guide=FALSE) + 
  scale_shape_discrete(guide=FALSE) + 
  scale_color_manual(name = "", 
                     labels = c("baseline", "baseline+", "continuous","velox", "retraining"),
                     values = c("a"="black", "b"="black","c"="black","d"="black", "e"="black"))+
  guides(color=guide_legend(override.aes=list(shape=c(NA,NA,NA,NA,18),linetype=c(3,4,1,5,0)))) 

ggsave(p , filename = 'higgs/higgs-quality.eps', 
       device = 'eps', 
       width = 14, height = 5, 
       units = "in")

#### URL ####
# OLD
#continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-50/offline-step-1.0/online-step-0.1/2017-03-28-13-50/error-rates.txt', header = FALSE, col.names = 'continuous')
#velox = read.csv('url-reputation/velox/num-iterations-500/slack-640/offline-step-1.0/online-step-0.1/2017-03-29-15-04/error-rates.txt', header = FALSE, col.names = 'velox')
#baselinePlus = read.csv('url-reputation/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-03-30-10-32/error-rates.txt', header = FALSE, col.names = 'baselinePlus')
#baseline= read.csv('url-reputation/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-03-28-18-05/error-rates.txt', header = FALSE, col.names = 'baseline')

# NEW step 0.1
continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-200/offline-step-1.0/online-step-0.1/2017-04-24-00-41/error-rates.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('url-reputation/velox/num-iterations-500/slack-1280/offline-step-1.0/online-step-0.1/2017-04-12-02-46/error-rates.txt', header = FALSE, col.names = 'velox')
baselinePlus = read.csv('url-reputation/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-04-24-05-46/error-rates.txt', header = FALSE, col.names = 'baselinePlus')
baseline= read.csv('url-reputation/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-04-24-12-11/error-rates.txt', header = FALSE, col.names = 'baseline')


# NEW step 0.01
#continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-40/offline-step-1.0/online-step-0.01/continuous-step-0.2/2017-04-30-19-28/error-rates.txt', header = FALSE, col.names = 'continuous')
#velox = read.csv('url-reputation/velox/num-iterations-500/slack-1280/offline-step-1.0/online-step-0.01/2017-04-25-18-46/error-rates.txt', header = FALSE, col.names = 'velox')
#baselinePlus = read.csv('url-reputation/baseline-plus/num-iterations-500/slack-none/offline-step-1.0/online-step-0.01/2017-04-24-15-55/error-rates.txt', header = FALSE, col.names = 'baselinePlus')
#baseline= read.csv('url-reputation/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-1.0/2017-04-24-12-11/error-rates.txt', header = FALSE, col.names = 'baseline')

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

retrainings = seq(300, 3600, 320)

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
  #ylim(c(0.367,0.381)) + 
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

ggsave(p , filename = 'url-reputation/url-reputation-quality.eps', 
       device = 'eps', 
       width = 14, height = 5, 
       units = "in")


###################### TOTAL TRAINING TIMES #####################

#### HIGGS ####
continuous = read.csv(paste(HIGGS_CONTINUOUS,'training-times.txt', sep='/'), header = FALSE, col.names = 'continuous')
velox = read.csv(paste(HIGGS_VELOX,'training-times.txt', sep='/'), header = FALSE, col.names = 'velox')
baseline = continuous[[1]][1]
continuous = sum(continuous)
velox = sum(velox)
methods = c('Baseline', 'Continuous', 'Velox')
df = data.frame(methods = methods, time = c(baseline, continuous, velox)/60000)
melted = melt(df, id.vars = 'methods', variable.names = "methods")
melted$methods = factor(as.character(melted$methods), 
                        levels=c("Baseline","Continuous","Velox"))

higgsTime = 
  ggplot(melted, aes(x = methods, y = value)) +
  geom_bar(stat='identity') + 
  xlab("") + ylab("Time (m)") + 
  scale_y_continuous(expand = c(0, 0), limits = c(0, 32)) +
  theme_bw() + 
  theme(legend.position="none",
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color = "black"),  
        plot.margin = unit(c(0.4, 0.0, -0.80, 0.2), "cm"))

ggsave(higgsTime , filename = 'higgs/higgs-times.eps', 
       device = 'eps',
       width = 7, height = 5, 
       units = "in")

#### URL REPUTATION ####
continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-50/offline-step-1.0/online-step-0.1/2017-03-28-13-50/training-times.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('url-reputation/velox/num-iterations-500/slack-640/offline-step-1.0/online-step-0.1/2017-03-29-15-04/training-times.txt', header = FALSE, col.names = 'velox')

baseline = continuous[[1]][1]
continuous = sum(continuous)
velox = sum(velox)
methods = c('Baseline', 'Continuous', 'Velox')
df = data.frame(methods = methods, time = c(baseline, continuous, velox)/60000)
melted = melt(df, id.vars = 'methods', variable.names = "methods")
melted$methods = factor(as.character(melted$methods), 
                        levels=c("Baseline","Continuous","Velox"))

urlTime = 
  ggplot(melted, aes(x = methods, y = value)) +
  geom_bar(stat='identity') + 
  xlab("") + ylab("Time (m)") + 
  scale_y_continuous(expand = c(0, 0), limits = c(0, 625)) +
  theme_bw() + 
  theme(legend.position="none",
        axis.text=element_text(size=28, color = "black"),
        axis.title=element_text(size=28, color = "black"),  
        plot.margin = unit(c(0.4, 0.0, -0.80, 0.2), "cm"))

ggsave(urlTime , filename = 'url-reputation/url-reputation-times.eps', 
       device = 'eps',
       width = 7, height = 5, 
       units = "in")

###################### META: PERFORMANCE VS QUALITY #####################

#### HIGGS ####
continuousTime = read.csv(paste(HIGGS_CONTINUOUS,'training-times.txt', sep='/'), header = FALSE, col.names = 'continuous')
veloxTime = read.csv(paste(HIGGS_VELOX,'training-times.txt', sep='/'), header = FALSE, col.names = 'velox')
baselineTime = continuousTime[[1]][1]

continuous = read.csv(paste(HIGGS_CONTINUOUS,'error-rates.txt', sep = '/'), header = FALSE, col.names = 'continuous')
velox = read.csv(paste(HIGGS_VELOX,'error-rates.txt', sep = '/'), header = FALSE, col.names = 'velox')
baseline = read.csv(paste(HIGGS_BASELINE,'error-rates.txt', sep = '/'), header = FALSE)


df = data.frame('error'=c(colMeans(continuous), colMeans(velox), colMeans(baseline)), 
                'time' = c(sum(continuousTime)/60000, sum(veloxTime)/60000, baselineTime/60000), 
                'models'=c('Continuous', 'Velox', 'Baseline'))
p = 
  ggplot(data = df, aes(x = time, y = error)) + 
  geom_point(aes(shape = models),  lwd = 12) + 
  #geom_text(aes(label = models, colour = models), size = 5, fontface ="bold", hjust="inward", vjust="inward", show.legend  = F, angle = 45)  + 
  xlab("Time (m)") + ylab("Avg Error rate") + 
  #xlim(c(0,400)) + 
  ylim(c(0.367, 0.38)) + 
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

ggsave(p , filename = 'higgs/higgs-meta-performance.eps', 
       device = cairo_ps,
       width = 7, height = 5, 
       units = "in")

#### URL REPUTATION ####
continuousTime = read.csv('url-reputation/continuous/num-iterations-500/slack-50/offline-step-1.0/online-step-0.1/2017-03-28-13-50/training-times.txt', header = FALSE, col.names = 'continuous')
veloxTime = read.csv('url-reputation/velox/num-iterations-500/slack-640/offline-step-1.0/online-step-0.1/2017-03-29-15-04/training-times.txt', header = FALSE, col.names = 'velox')
baselineTime = continuousTime[[1]][1]

continuous = read.csv('url-reputation/continuous/num-iterations-500/slack-50/offline-step-1.0/online-step-0.1/2017-03-28-13-50/error-rates.txt', header = FALSE, col.names = 'continuous')
velox = read.csv('url-reputation/velox/num-iterations-500/slack-640/offline-step-1.0/online-step-0.1/2017-03-29-15-04/error-rates.txt', header = FALSE, col.names = 'velox')
baseline = read.csv('url-reputation/baseline/num-iterations-500/slack-none/offline-step-1.0/online-step-0.1/2017-03-28-18-05/error-rates.txt', header = FALSE)


df = data.frame('error'=c(colMeans(continuous), colMeans(velox), colMeans(baseline)), 
                'time' = c(sum(continuousTime)/60000, sum(veloxTime)/60000, baselineTime/60000), 
                'models'=c('Continuous', 'Velox', 'Baseline'))
p = 
  ggplot(data = df, aes(x = time, y = error)) + 
  geom_point(aes(shape = models),  lwd = 12) + 
  #geom_text(aes(label = models, colour = models), size = 5, fontface ="bold", hjust="inward", vjust="inward", show.legend  = F, angle = 45)  + 
  xlab("Time (m)") + ylab("Avg Error rate") + 
  #xlim(c(0,400)) + 
  ylim(c(0.034, 0.044)) + 
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

ggsave(p , filename = 'url-reputation/url-reputation-meta-performance.eps', 
       device = cairo_ps,
       width = 7, height = 5, 
       units = "in")


