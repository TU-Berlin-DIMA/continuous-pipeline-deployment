setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)


urlDataProcessing <- function(){
  online = cumsum(read.csv('deployment-modes-quality-time/confusion_matrix-online', header = FALSE, col.names = c('tp','fp','tn','fn')))
  online$mc = (online$fp + online$fn) / (online$fp + online$fn + online$tp + online$tn)
  
  continuous = cumsum(read.csv('deployment-modes-quality-time/confusion_matrix-time_based-100-with-optimization', header = FALSE, col.names = c('tp','fp','tn','fn')))
  continuous$mc = (continuous$fp + continuous$fn) / (continuous$fp + continuous$fn + continuous$tp + continuous$tn)
  
  baseline = cumsum(read.csv('deployment-modes-quality-time/confusion_matrix-baseline', header = FALSE, col.names = c('tp','fp','tn','fn')))
  baseline$mc = (baseline$fp + baseline$fn) / (baseline$fp + baseline$fn + baseline$tp + baseline$tn)
  
  periodical = cumsum(read.csv('deployment-modes-quality-time/confusion_matrix-periodical-warm', header = FALSE, col.names = c('tp','fp','tn','fn')))
  periodical$mc = (periodical$fp + periodical$fn) / (periodical$fp + periodical$fn + periodical$tp + periodical$tn)
  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = nrow(periodical)
  df = data.frame(Time = 1:nrow(periodical), 
                  online = append(online$mc,maxLength), 
                  continuous =  append(continuous$mc,maxLength),
                  #baseline = baseline$mc,
                  periodical = append(periodical$mc,  maxLength))
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  df$online = df$online * 100
  df$continuous = df$continuous * 100
  #df$baseline = df$baseline * 100
  df$periodical = df$periodical * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

criteoDataProcessing <- function(){
  
}

taxiDataProcessing <- function(){
  
}


urlData = urlDataProcessing()
urlBreaks = c(1,3000, 6000 ,9000, 12000)
urlLabels = c("day 1","day 30", "day 60", "day 90","day 120")
criteoData = urlDataProcessing()
taxiData = urlDataProcessing()
taxiBreaks = c(1,3000, 6000 ,9000, 12000)
taxiLabels = c("Feb 2015","July 2015", "Jan 2016", "July 206", "Dec 2016")
criteoBreaks = c(1,3000, 6000 ,9000, 12000)
criteoLabels = c("day 1","day 3", "day 6", "day 9","day 12")


fontLabelSize = 10
baseSize = 14


url_plot = ggline(urlData, 'Time', 'value', ylab = "Misclassification (\\%)", xlab = '(a) URL',
                  shape = '-1', size = 1, color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks, labels= urlLabels)
url_plot = ggpar(url_plot, legend = "top", legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(axis.text.x = element_text(margin = margin(t=-1)))


taxi_plot = ggline(taxiData, 'Time', 'value', ylab = "MSLE", xlab = '(b) Taxi',
                  shape = '-1',size =1, color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks, labels= taxiLabels) + rremove('legend')
taxi_plot = ggpar(taxi_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(axis.text.x = element_text(margin = margin(t=-1)))

criteo_plot = ggline(criteoData, 'Time', 'value', ylab = "MSE", xlab = '(c) Criteo',
                     shape = '-1',size =1, color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = criteoBreaks, labels= criteoLabels) + rremove('legend')
criteo_plot = ggpar(criteo_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(axis.text.x = element_text(margin = margin(t=-1)))

deployment_quality = ggarrange(url_plot,taxi_plot,criteo_plot, nrow = 3, ncol = 1, common.legend = TRUE) + rremove('legend') 

tikz(file = "../../images/experiment-results/tikz/deployment-quality-experiment.tex", width = 4, height = 4)
deployment_quality 
dev.off()
