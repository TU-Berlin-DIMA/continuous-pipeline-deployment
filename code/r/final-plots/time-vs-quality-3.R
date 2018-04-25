setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)


urlQualityProcessing <- function(){
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

taxiTimeProcessing <- function(){
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


urlQuality = urlQualityProcessing()
urlTime = urlTimeProcessing()
urlBreaks = c(1,3000, 6000 ,9000, 12000)
urlLabels = c("day1","day30", "day60", "day90","day120")

taxiQuality = taxiQualityProcessing()
taxiTime = taxiTimeProcessing()
taxiBreaks = c(1,3000, 6000 ,9000, 12000)
taxiLabels = c("Feb15","Jul15", "Jan16", "Jul16", "Dec16")

criteoQuality = criteoQualityProcessing()
criteoTime = criteoTimeProcessing()
criteoBreaks = c(1,3000, 6000 ,9000, 12000)
criteoLabels = c("day1","day3", "day6", "day9","day12")



fontLabelSize = 10
baseSize = 14


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

urlTimePlot = ggline(urlTime, 'Time', 'value', ylab = "Deployment Cost (minutes)", xlab = '(a) URL',
                        shape = '-1', size = 1, linetype ='Deployment', color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks, labels= urlLabels)
urlTimePlot = ggpar(urlTimePlot, legend = "top", legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

taxiQualityPlot = ggline(taxiQuality, 'Time', 'value', ylab = "RMSLE", xlab = '(b) Taxi',
                         # change size = 0 when we have real data
                  shape = '-1',size =0, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks, labels= taxiLabels) + rremove('legend')
taxiQualityPlot = ggpar(taxiQualityPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

taxiTimePlot = ggline(taxiTime, 'Time', 'value', ylab = "Deployment Cost (minutes)", xlab = '(b) Taxi',
                      # change size = 0 when we have real data
                         shape = '-1',size =0, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks, labels= taxiLabels) + rremove('legend')
taxiTimePlot = ggpar(taxiTimePlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))


criteoQualityPlot = ggline(criteoQuality, 'Time', 'value', ylab = "Deployment Cost (minutes)", xlab = '(c) Criteo',
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

criteoTimePlot = ggline(criteoTime, 'Time', 'value', ylab = "MSE", xlab = '(c) Criteo',
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

#tikz(file = "../images/experiment-results/tikz/deployment-quality-experiment.tex", width = 4, height = 4)

#dev.off()
