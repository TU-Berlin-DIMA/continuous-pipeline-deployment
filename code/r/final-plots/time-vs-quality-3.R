setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)
source('../code/r/final-plots/functions.r')

urlQualityProcessing <- function(){
  Online = getMisclassification('url-reputation/deployment-modes-quality-time/confusion_matrix-online')
  Continuous = getMisclassification('url-reputation/deployment-modes-quality-time/confusion_matrix-time_based-100-with-optimization')
  Baseline = getMisclassification('url-reputation/deployment-modes-quality-time/confusion_matrix-baseline')
  Periodical = getMisclassification('url-reputation/deployment-modes-quality-time/confusion_matrix-periodical-warm')

  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = length(Periodical)
  df = data.frame(Time = 1:length(Periodical), Continuous, Periodical, Online )
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  df$Online = df$Online * 100
  df$Continuous = df$Continuous * 100
  df$Periodical = df$Periodical * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

urlTimeProcessing <- function(){
  scale = 1000 * 60
  oTime = read.csv('url-reputation/deployment-modes-quality-time/online-time', header = FALSE, col.names = c('time'))$time / scale
  cTime = read.csv('url-reputation/deployment-modes-quality-time/continuous-full-optimization-time', header = FALSE, col.names = c('time'))$time / scale
  bTime = read.csv('url-reputation/deployment-modes-quality-time/baseline-time', header = FALSE, col.names = c('time'))$time / scale
  pTime = read.csv('url-reputation/deployment-modes-quality-time/periodical-warm-time', header = FALSE, col.names = c('time'))$time / scale
  
  trainingTime = pTime - oTime 
  # Cost Increase
  baselineTime = cumsum(rep(bTime/12000,12000))
  continuousTime = cumsum(rep(cTime/12000,12000))
  onlineTime = cumsum(rep(oTime/12000,12000))
  periodicalTime = rep(oTime/12000,12000)
  
  trainingPeriodical = rep(trainingTime/12,12)
  periodicalTime[c(1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,11000,12000)] = periodicalTime[c(1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,11000,12000)] + trainingPeriodical
  periodicalTime = cumsum(periodicalTime)
  
  df = data.frame(Time = 1:length(periodicalTime), 
                  Continuous = continuousTime,
                  Periodical = periodicalTime,
                  Online = onlineTime)
                 # Baseline = baselineTime)
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

criteoQualityProcessing <- function(){
  online = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-online', header = FALSE, col.names = c('tp','fp','tn','fn')))
  online$mc = (online$fp + online$fn) / (online$fp + online$fn + online$tp + online$tn)
  
  continuous = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-time_based-100-with-optimization', header = FALSE, col.names = c('tp','fp','tn','fn')))
  continuous$mc = (continuous$fp + continuous$fn) / (continuous$fp + continuous$fn + continuous$tp + continuous$tn)
  
  baseline = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-baseline', header = FALSE, col.names = c('tp','fp','tn','fn')))
  baseline$mc = (baseline$fp + baseline$fn) / (baseline$fp + baseline$fn + baseline$tp + baseline$tn)
  
  periodical = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-periodical-warm', header = FALSE, col.names = c('tp','fp','tn','fn')))
  periodical$mc = (periodical$fp + periodical$fn) / (periodical$fp + periodical$fn + periodical$tp + periodical$tn)
  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = nrow(periodical)
  df = data.frame(Time = 1:nrow(periodical), 
                  Continuous =  continuous$mc,
                  Periodical = periodical$mc,
                  Online = online$mc)
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  df$Online = df$Online * 100
  df$Continuous = df$Continuous * 100
  #df$baseline = df$baseline * 100
  df$Periodical = df$Periodical * 100

  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
  
}

criteoTimeProcessing <- function(){
  scale = 1000 * 60
  oTime = read.csv('url-reputation/deployment-modes-quality-time/online-time', header = FALSE, col.names = c('time'))$time / scale
  cTime = read.csv('url-reputation/deployment-modes-quality-time/continuous-full-optimization-time', header = FALSE, col.names = c('time'))$time / scale
  bTime = read.csv('url-reputation/deployment-modes-quality-time/baseline-time', header = FALSE, col.names = c('time'))$time / scale
  pTime = read.csv('url-reputation/deployment-modes-quality-time/periodical-warm-time', header = FALSE, col.names = c('time'))$time / scale
  trainingTime = pTime - oTime 
  
  
  # Cost Increase
  baselineTime = cumsum(rep(bTime/12000,12000))
  continuousTime = cumsum(rep(cTime/12000,12000))
  onlineTime = cumsum(rep(oTime/12000,12000))
  periodicalTime = rep(oTime/12000,12000)
  
  trainingPeriodical = rep(trainingTime/12,12)
  periodicalTime[c(1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,11000,12000)] = periodicalTime[c(1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,11000,12000)] + trainingPeriodical
  periodicalTime = cumsum(periodicalTime)
  
  df = data.frame(Time = 1:length(periodicalTime), 
                  Continuous = continuousTime,
                  Periodical = periodicalTime,
                  Online = onlineTime)
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

taxiQualityProcessing <- function(){
  Online = getRMSLE('nyc-taxi/deployment-modes/online/rmsle')  
  Continuous = getRMSLE('nyc-taxi/deployment-modes/continuous-with-optimization-time_based-720/rmsle')
  Baseline = getRMSLE('nyc-taxi/deployment-modes/baseline/rmsle')
  Periodical = getRMSLE('nyc-taxi/deployment-modes/periodical-with-warmstarting/rmsle')
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = length(Periodical)
  df = data.frame(Time = 1:maxLength, Continuous, Periodical, Online)
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0)), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

taxiTimeProcessing <- function(){
  scale = 1000 * 60
  Online = cumsum(read.csv('nyc-taxi/deployment-modes/online/time', header = FALSE, col.names = c('time'))$time) / scale
  Continuous = cumsum(read.csv('nyc-taxi/deployment-modes/continuous-with-optimization-time_based-720/time', header = FALSE, col.names = c('time'))$time) / scale
  Baseline = cumsum(read.csv('nyc-taxi/deployment-modes/baseline/time', header = FALSE, col.names = c('time'))$time) / scale
  Periodical = cumsum(read.csv('nyc-taxi/deployment-modes/periodical-with-warmstarting/time', header = FALSE, col.names = c('time'))$time) / scale
  last = tail(Periodical,1)
  Periodical = Periodical[1:length(Baseline)]
  Periodical[length(Periodical)] = last
  
  df = data.frame(Time = 1:length(Periodical), Continuous, Periodical, Online)
  DAY_DURATION = 500
  df = df[(df$Time %% DAY_DURATION == 0), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

fontLabelSize = 8
baseSize = 10

urlQuality = urlQualityProcessing()
urlTime = urlTimeProcessing()
urlBreaks = c(1,3000, 6000 ,9000, 12000)
urlLabels = c("day1","day30", "day60", "day90","day120")

urlQualityPlot = ggline(urlQuality, 'Time', 'value', ylab = "Misclassification (\\%)", xlab = '(a) URL',
                 shape = '-1', size = 1, linetype ='Deployment', color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks, labels= urlLabels)
urlQualityPlot = ggpar(urlQualityPlot, legend = "top", legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

urlTimePlot = ggline(urlTime, 'Time', 'value', ylab = "Time (m)", xlab = '(a) URL',
                        shape = '-1', size = 1, linetype ='Deployment', color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks, labels= urlLabels)
urlTimePlot = ggpar(urlTimePlot, legend = "top", legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

taxiQuality = taxiQualityProcessing()
taxiTime = taxiTimeProcessing()
taxiBreaks = c(500,4000, 8000, 12300)
taxiLabels = c("Feb15","Jul15", "Jan16", "June16")
taxiQualityPlot = ggline(taxiQuality, 'Time', 'value', ylab = "RMSLE", xlab = '(b) Taxi',
                  shape = '-1',size = 1, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks, labels= taxiLabels) + rremove('legend')
taxiQualityPlot = ggpar(taxiQualityPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

taxiTimePlot = ggline(taxiTime, 'Time', 'value', ylab = "Time (m)", xlab = '(b) Taxi',
                         shape = '-1',size = 1, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks, labels= taxiLabels) + rremove('legend')
taxiTimePlot = ggpar(taxiTimePlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

criteoQuality = criteoQualityProcessing()
criteoTime = criteoTimeProcessing()
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
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

criteoTimePlot = ggline(criteoTime, 'Time', 'value', ylab = "Time (m)", xlab = '(c) Criteo',
                        # change size = 0 when we have real data
                           shape = '-1',size =0, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) +  
  scale_x_continuous(breaks = criteoBreaks, labels= criteoLabels) + rremove('legend')
criteoTimePlot = ggpar(criteoTimePlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

deploymentQuality = ggarrange(urlQualityPlot,urlTimePlot, taxiQualityPlot, taxiTimePlot, criteoQualityPlot, criteoTimePlot, nrow = 3, ncol = 2, common.legend = TRUE)
tikz(file = "../images/experiment-results/tikz/deployment-quality-and-time-experiment.tex", width = 6, height = 3)
deploymentQuality
dev.off()
