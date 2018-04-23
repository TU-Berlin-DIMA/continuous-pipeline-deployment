setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)



urlDataProcessing <- function(){
  DAY_DURATION = 100
 
  scale = 1000 * 60
  online = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-online', header = FALSE, col.names = c('tp','fp','tn','fn')))
  online$mc = (online$fp + online$fn) / (online$fp + online$fn + online$tp + online$tn)
  oTime = read.csv('url-reputation/deployment-modes-quality-time/online-time', header = FALSE, col.names = c('time'))$time / scale
  
  
  continuous = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-time_based-100-with-optimization', header = FALSE, col.names = c('tp','fp','tn','fn')))
  continuous$mc = (continuous$fp + continuous$fn) / (continuous$fp + continuous$fn + continuous$tp + continuous$tn)
  cTime = read.csv('url-reputation/deployment-modes-quality-time/continuous-full-optimization-time', header = FALSE, col.names = c('time'))$time / scale
  
  baseline = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-baseline', header = FALSE, col.names = c('tp','fp','tn','fn')))
  baseline$mc = (baseline$fp + baseline$fn) / (baseline$fp + baseline$fn + baseline$tp + baseline$tn)
  bTime = read.csv('url-reputation/deployment-modes-quality-time/baseline-time', header = FALSE, col.names = c('time'))$time / scale
  
  periodical = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-periodical-warm', header = FALSE, col.names = c('tp','fp','tn','fn')))
  periodical$mc = (periodical$fp + periodical$fn) / (periodical$fp + periodical$fn + periodical$tp + periodical$tn)
  pTime = read.csv('url-reputation/deployment-modes-quality-time/periodical-warm-time', header = FALSE, col.names = c('time'))$time / scale
  trainingTime = pTime - oTime 

  df = data.frame(Time = 1:nrow(periodical), 
                  Continuous = continuous$mc,
                  Periodical = periodical$mc,
                  Baseline = baseline$mc, 
                  Oracle = rep(0,nrow(periodical)))
  
  # Quality improvement
  df[c('Continuous','Periodical','Baseline', 'Oracle')]  = (baseline$mc - df[c('Continuous','Periodical','Baseline','Oracle')]) / baseline$mc
  df = df[(df$Time %% DAY_DURATION == 0), ]
  df$Time = seq(1,120)
 
  # Cost Increase
  baselineTime = cumsum(rep(bTime/120,120))
  continuousTime = ((cumsum(rep(cTime/120,120)) - baselineTime)/baselineTime) 
  periodicalTime = cumsum(rep(oTime/120,120))
  
  trainingPeriodical = rep(trainingTime/12,12)
  periodicalTime[c(10,20,30,40,50,60,70,80,90,100,110,120)] = periodicalTime[c(10,20,30,40,50,60,70,80,90,100,110,120)] + trainingPeriodical
  periodicalTime = (cumsum(periodicalTime)- baselineTime)/baselineTime
  
  
  df$Continuous = 100* df$Continuous/continuousTime
  df$Oracle = 100 * df$Oracle
  df$Periodical = 100* df$Periodical/periodicalTime
  df$Baseline = 100*df$Baseline

  df$Oracle = NULL
  
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

taxiDataProcessing <- function(){
  DAY_DURATION = 100
  
  scale = 1000 * 60
  online = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-online', header = FALSE, col.names = c('tp','fp','tn','fn')))
  online$mc = (online$fp + online$fn) / (online$fp + online$fn + online$tp + online$tn)
  oTime = read.csv('url-reputation/deployment-modes-quality-time/online-time', header = FALSE, col.names = c('time'))$time / scale
  
  
  continuous = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-time_based-100-with-optimization', header = FALSE, col.names = c('tp','fp','tn','fn')))
  continuous$mc = (continuous$fp + continuous$fn) / (continuous$fp + continuous$fn + continuous$tp + continuous$tn)
  cTime = read.csv('url-reputation/deployment-modes-quality-time/continuous-full-optimization-time', header = FALSE, col.names = c('time'))$time / scale
  
  baseline = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-baseline', header = FALSE, col.names = c('tp','fp','tn','fn')))
  baseline$mc = (baseline$fp + baseline$fn) / (baseline$fp + baseline$fn + baseline$tp + baseline$tn)
  bTime = read.csv('url-reputation/deployment-modes-quality-time/baseline-time', header = FALSE, col.names = c('time'))$time / scale
  
  periodical = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-periodical-warm', header = FALSE, col.names = c('tp','fp','tn','fn')))
  periodical$mc = (periodical$fp + periodical$fn) / (periodical$fp + periodical$fn + periodical$tp + periodical$tn)
  pTime = read.csv('url-reputation/deployment-modes-quality-time/periodical-warm-time', header = FALSE, col.names = c('time'))$time / scale
  trainingTime = pTime - oTime 
  
  df = data.frame(Time = 1:nrow(periodical), 
                  Continuous = continuous$mc,
                  Periodical = periodical$mc,
                  Baseline = baseline$mc, 
                  Oracle = rep(0,nrow(periodical)))
  
  # Quality improvement
  df[c('Continuous','Periodical','Baseline', 'Oracle')]  = (baseline$mc - df[c('Continuous','Periodical','Baseline','Oracle')]) / baseline$mc
  df = df[(df$Time %% DAY_DURATION == 0), ]
  df$Time = seq(1,120)
  
  # Cost Increase
  baselineTime = cumsum(rep(bTime/120,120))
  continuousTime = ((cumsum(rep(cTime/120,120)) - baselineTime)/baselineTime) 
  periodicalTime = cumsum(rep(oTime/120,120))
  
  trainingPeriodical = rep(trainingTime/12,12)
  periodicalTime[c(10,20,30,40,50,60,70,80,90,100,110,120)] = periodicalTime[c(10,20,30,40,50,60,70,80,90,100,110,120)] + trainingPeriodical
  periodicalTime = (cumsum(periodicalTime)- baselineTime)/baselineTime
  
  
  df$Continuous = 100* df$Continuous/continuousTime
  df$Oracle = 100 * df$Oracle
  df$Periodical = 100* df$Periodical/periodicalTime
  df$Baseline = 100*df$Baseline
  
  df$Oracle = NULL
  
  df[,c(2,3,4,5)] = 0
  
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}


criteoDataProcessing <- function(){
  DAY_DURATION = 100
  
  scale = 1000 * 60
  online = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-online', header = FALSE, col.names = c('tp','fp','tn','fn')))
  online$mc = (online$fp + online$fn) / (online$fp + online$fn + online$tp + online$tn)
  oTime = read.csv('url-reputation/deployment-modes-quality-time/online-time', header = FALSE, col.names = c('time'))$time / scale
  
  
  continuous = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-time_based-100-with-optimization', header = FALSE, col.names = c('tp','fp','tn','fn')))
  continuous$mc = (continuous$fp + continuous$fn) / (continuous$fp + continuous$fn + continuous$tp + continuous$tn)
  cTime = read.csv('url-reputation/deployment-modes-quality-time/continuous-full-optimization-time', header = FALSE, col.names = c('time'))$time / scale
  
  baseline = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-baseline', header = FALSE, col.names = c('tp','fp','tn','fn')))
  baseline$mc = (baseline$fp + baseline$fn) / (baseline$fp + baseline$fn + baseline$tp + baseline$tn)
  bTime = read.csv('url-reputation/deployment-modes-quality-time/baseline-time', header = FALSE, col.names = c('time'))$time / scale
  
  periodical = cumsum(read.csv('url-reputation/deployment-modes-quality-time/confusion_matrix-periodical-warm', header = FALSE, col.names = c('tp','fp','tn','fn')))
  periodical$mc = (periodical$fp + periodical$fn) / (periodical$fp + periodical$fn + periodical$tp + periodical$tn)
  pTime = read.csv('url-reputation/deployment-modes-quality-time/periodical-warm-time', header = FALSE, col.names = c('time'))$time / scale
  trainingTime = pTime - oTime 
  
  df = data.frame(Time = 1:nrow(periodical), 
                  Continuous = continuous$mc,
                  Periodical = periodical$mc,
                  Baseline = baseline$mc, 
                  Oracle = rep(0,nrow(periodical)))
  
  # Quality improvement
  df[c('Continuous','Periodical','Baseline', 'Oracle')]  = (baseline$mc - df[c('Continuous','Periodical','Baseline','Oracle')]) / baseline$mc
  df = df[(df$Time %% DAY_DURATION == 0), ]
  df$Time = seq(1,120)
  
  # Cost Increase
  baselineTime = cumsum(rep(bTime/120,120))
  continuousTime = ((cumsum(rep(cTime/120,120)) - baselineTime)/baselineTime) 
  periodicalTime = cumsum(rep(oTime/120,120))
  
  trainingPeriodical = rep(trainingTime/12,12)
  periodicalTime[c(10,20,30,40,50,60,70,80,90,100,110,120)] = periodicalTime[c(10,20,30,40,50,60,70,80,90,100,110,120)] + trainingPeriodical
  periodicalTime = (cumsum(periodicalTime)- baselineTime)/baselineTime
  
  
  df$Continuous = 100* df$Continuous/continuousTime
  df$Oracle = 100 * df$Oracle
  df$Periodical = 100* df$Periodical/periodicalTime
  df$Baseline = 100*df$Baseline
  
  df$Oracle = NULL
  
  df[,c(2,3,4,5)] = 0
  
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

urlData = urlDataProcessing()
urlBreaks = c(1,30, 60 ,90, 120)
urlLabels = c("day1","day30", "day60", "day90","day120")

taxiData = taxiDataProcessing()
taxiBreaks = c(1,30, 60 ,90, 120)
taxiLabels = c("Feb15","Jul15", "Jan16", "Jul16", "Dec16")

criteoData = criteoDataProcessing()
criteoBreaks = c(1,30, 60 ,90, 120)
criteoLabels = c("day1","day3", "day6", "day9","day12")



fontLabelSize = 10
baseSize = 14


urlPlot = ggline(urlData, 'Time', 'value', ylab = "", xlab = '(a) URL',
                 shape = '-1', size = 1, linetype ='Deployment',yscale = "log10", color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks, labels= urlLabels)
urlPlot = ggpar(urlPlot, legend = "top", legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))


taxiPlot = ggline(taxiData, 'Time', 'value', ylab = "Performance Indicator (PI)", xlab = '(b) Taxi',
                  shape = '-1',size =1, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize),
                  ylim=c(min(urlData$value),max(urlData$value))) + 
  scale_x_continuous(breaks = taxiBreaks, labels= taxiLabels) + rremove('legend')
taxiPlot = ggpar(taxiPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

criteoPlot = ggline(criteoData, 'Time', 'value', ylab = "", xlab = '(c) Criteo',
                    shape = '-1',size =1, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize),
                    ylim=c(min(urlData$value),max(urlData$value))) + 
  scale_x_continuous(breaks = criteoBreaks, labels= criteoLabels) + rremove('legend')
criteoPlot = ggpar(criteoPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))


timeVsQuality = ggarrange(urlPlot, taxiPlot, criteoPlot, nrow = 3, ncol = 1, common.legend = TRUE)

tikz(file = "../images/experiment-results/tikz/quality-vs-time.tex", width = 4, height = 4)
timeVsQuality 
dev.off()
