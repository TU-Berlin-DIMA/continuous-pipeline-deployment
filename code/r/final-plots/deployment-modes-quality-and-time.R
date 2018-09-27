setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)
source('../code/r/final-plots/functions.r')

urlQualityProcessing <- function(){
  Online = getMisclassification('url-reputation/deployment-modes/online/confusion_matrix')
  Continuous = getMisclassification('url-reputation/deployment-modes/continuous-with-optimization-time_based-100/confusion_matrix')
  Baseline = getMisclassification('url-reputation/final/deployment-modes/baseline/confusion_matrix')
  Periodical = getMisclassification('url-reputation/final/deployment-modes/periodical-with-warmstarting/confusion_matrix')

  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = length(Online)
  df = data.frame(Time = 1:length(Online), Continuous, Periodical = append(Periodical, maxLength), Online )
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 200), ]
  df$Online = df$Online * 100
  df$Continuous = df$Continuous * 100
  df$Periodical = df$Periodical * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

urlTimeProcessing <- function(){
  scale = 1000 * 60
  Online = cumsum(read.csv('url-reputation/deployment-modes/online/time', header = FALSE, col.names = c('time'))$time) / scale
  Continuous = cumsum(read.csv('url-reputation/deployment-modes/continuous-with-optimization-time_based-100/time', header = FALSE, col.names = c('time'))$time) / scale
  Baseline = cumsum(read.csv('url-reputation/deployment-modes/baseline/time', header = FALSE, col.names = c('time'))$time) / scale
  Periodical = cumsum(read.csv('url-reputation/deployment-modes/periodical-with-warmstarting/time', header = FALSE, col.names = c('time'))$time) / scale
  last = tail(Periodical,1)
  Periodical = Periodical[1:length(Online)]
  Periodical[length(Periodical)] = last
  
  df = data.frame(Time = 1:length(Online), Continuous, Periodical, Online)
  DAY_DURATION = 500
  df = df[(df$Time %% DAY_DURATION == 0), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

criteoQualityProcessing <- function(){
  Online = getMisclassification('url-reputation/deployment-modes/online/confusion_matrix')
  Continuous = getMisclassification('url-reputation/deployment-modes/continuous-with-optimization-time_based-100/confusion_matrix')
  Baseline = getMisclassification('url-reputation/final/deployment-modes/baseline/confusion_matrix')
  Periodical = getMisclassification('url-reputation/final/deployment-modes/periodical-with-warmstarting/confusion_matrix')
  
  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = length(Periodical)
  df = data.frame(Time = 1:length(Periodical), Continuous, Periodical, Online )
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0)), ]
  df$Online = df$Online * 100
  df$Continuous = df$Continuous * 100
  df$Periodical = df$Periodical * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

criteoTimeProcessing <- function(){
  scale = 1000 * 60
  Online = cumsum(read.csv('url-reputation/final/deployment-modes/online/time', header = FALSE, col.names = c('time'))$time) / scale
  Continuous = cumsum(read.csv('url-reputation/final/deployment-modes/continuous-with-optimization-time_based-100/time', header = FALSE, col.names = c('time'))$time) / scale
  Baseline = cumsum(read.csv('url-reputation//final/deployment-modes/baseline/time', header = FALSE, col.names = c('time'))$time) / scale
  Periodical = cumsum(read.csv('url-reputation-old//deployment-modes/periodical-with-warmstarting/time', header = FALSE, col.names = c('time'))$time) / scale
  last = tail(Periodical,1)
  Periodical = Periodical[1:length(Online)]
  Periodical[length(Periodical)] = last
  
  df = data.frame(Time = 1:length(Online), Continuous, Periodical, Online)
  DAY_DURATION = 500
  df = df[(df$Time %% DAY_DURATION == 0), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

taxiQualityProcessing <- function(){
  Online = getRMSLE('nyc-taxi/final/deployment-modes/online/rmsle')  
  Continuous = getRMSLE('nyc-taxi/final/deployment-modes/continuous-with-optimization-time_based-720/rmsle')
  Baseline = getRMSLE('nyc-taxi/final/deployment-modes/baseline/rmsle')
  Periodical = getRMSLE('nyc-taxi/final/deployment-modes/periodical-with-warmstarting/rmsle')
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = length(Online)
  df = data.frame(Time = 1:maxLength, Continuous, Periodical = append(Periodical, maxLength), Online)
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0)), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

taxiTimeProcessing <- function(){
  scale = 1000 * 60
  Online = cumsum(read.csv('nyc-taxi/final/deployment-modes/online/time', header = FALSE, col.names = c('time'))$time) / scale
  Continuous = cumsum(read.csv('nyc-taxi/final/deployment-modes/continuous-with-optimization-time_based-720/time', header = FALSE, col.names = c('time'))$time) / scale
  Baseline = cumsum(read.csv('nyc-taxi/final/deployment-modes/baseline/time', header = FALSE, col.names = c('time'))$time) / scale
  Periodical = cumsum(read.csv('nyc-taxi/final/deployment-modes/periodical-with-warmstarting/time', header = FALSE, col.names = c('time'))$time) / scale
  last = tail(Periodical,1)
  Periodical = Periodical[1:length(Baseline)]
  Periodical[length(Periodical)] = last
  
  df = data.frame(Time = 1:length(Online), Continuous, Periodical, Online)
  DAY_DURATION = 500
  df = df[(df$Time %% DAY_DURATION == 0), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

# For the paper use
fontLabelSize = 10
baseSize = 11
margin = -1

#For presentation use
#fontLabelSize = 12
#baseSize = 16
#margin = 2


####### URL PLOT ##########
urlQuality = urlQualityProcessing()
urlTime = urlTimeProcessing()
urlBreaks = c(500,3000, 6000 ,9000, 12000)
urlLabels = c("day1","day30", "day60", "day90","day120")

urlQualityPlot = ggline(urlQuality, 'Time', 'value', ylab = "Misclassification \\%", xlab = '(a) URL',
                 shape = '-1', size = 1, linetype ='Deployment', color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks, labels= urlLabels)
urlQualityPlot = ggpar(urlQualityPlot, legend = "top", legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=margin)),
        axis.text.x = element_text(margin = margin(t=margin)))

urlTimePlot = ggline(urlTime, 'Time', 'value', ylab = "Time (m)", xlab = '(b) URL',
                        shape = '-1', size = 1, linetype ='Deployment', color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks, labels= urlLabels)
urlTimePlot = ggpar(urlTimePlot, legend = "top", legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=margin)),
        axis.text.x = element_text(margin = margin(t=margin)))


####### TAXI PLOT ##########
taxiQuality = taxiQualityProcessing()
taxiTime = taxiTimeProcessing()
taxiBreaks = c(500,4000, 8000, 12300)
taxiLabels = c("Feb15","Jul15", "Jan16", "June16")
taxiQualityPlot = ggline(taxiQuality, 'Time', 'value', ylab = "RMSLE", xlab = '(c) Taxi',
                  shape = '-1',size = 1, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks, labels= taxiLabels) + rremove('legend')
taxiQualityPlot = ggpar(taxiQualityPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=margin)),
        axis.text.x = element_text(margin = margin(t=margin)))

taxiTimePlot = ggline(taxiTime, 'Time', 'value', ylab = "Time (m)", xlab = '(d) Taxi',
                         shape = '-1',size = 1, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks, labels= taxiLabels) + rremove('legend')
taxiTimePlot = ggpar(taxiTimePlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=margin)),
        axis.text.x = element_text(margin = margin(t=margin)))

####### CRITEO PLOT ##########
criteoQuality = urlQualityProcessing()
criteoTime = urlTimeProcessing()
criteoBreaks = c(1,3000, 6000 ,9000, 12000)
criteoLabels = c("day1","day3", "day6", "day9","day12")

criteoQualityPlot = ggline(criteoQuality, 'Time', 'value', ylab = "Log Loss", xlab = '(c) Criteo',
                           # change size = 0 when we have real data
                    shape = '-1',size =0, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) +  
  scale_x_continuous(breaks = criteoBreaks, labels= criteoLabels) + rremove('legend')
criteoQualityPlot = ggpar(criteoQualityPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=margin)),
        axis.text.x = element_text(margin = margin(t=margin)))

criteoTimePlot = ggline(criteoTime, 'Time', 'value', ylab = "Time (m)", xlab = '(c) Criteo',
                        # change size = 0 when we have real data
                           shape = '-1',size =0, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) +  
  scale_x_continuous(breaks = criteoBreaks, labels= criteoLabels) + rremove('legend')
criteoTimePlot = ggpar(criteoTimePlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=margin)),
        axis.text.x = element_text(margin = margin(t=margin)))

#deploymentQuality = ggarrange(urlQualityPlot,urlTimePlot, taxiQualityPlot, taxiTimePlot, criteoQualityPlot, criteoTimePlot, nrow = 3, ncol = 2, common.legend = TRUE)
deploymentQuality = ggarrange(urlQualityPlot,urlTimePlot, taxiQualityPlot, taxiTimePlot, nrow = 2, ncol = 2, common.legend = TRUE)


#ggsave(deploymentQuality, filename = '../images/experiment-results/eps/deployment-quality.eps', device = 'eps', width = 14, height = 6, units = "in")
tikz(file = "../images/experiment-results/tikz/deployment-quality-and-time-experiment.tex", width = 6, height = 4)
deploymentQuality
dev.off()
