setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)


urlDataProcessing <- function(){
  online = cumsum(read.csv('deployment-modes/confusion_matrix-online', header = FALSE, col.names = c('tp','fp','tn','fn')))
  online$mc = (online$fp + online$fn) / (online$fp + online$fn + online$tp + online$tn)
  
  continuous = cumsum(read.csv('deployment-modes/confusion_matrix-time_based-100-1iter', header = FALSE, col.names = c('tp','fp','tn','fn')))
  continuous$mc = (continuous$fp + continuous$fn) / (continuous$fp + continuous$fn + continuous$tp + continuous$tn)
  
  baseline = cumsum(read.csv('deployment-modes/confusion_matrix-baseline', header = FALSE, col.names = c('tp','fp','tn','fn')))
  baseline$mc = (baseline$fp + baseline$fn) / (baseline$fp + baseline$fn + baseline$tp + baseline$tn)
  
  periodical = cumsum(read.csv('deployment-modes/confusion_matrix-periodical-warmstart', header = FALSE, col.names = c('tp','fp','tn','fn')))
  periodical$mc = (periodical$fp + periodical$fn) / (periodical$fp + periodical$fn + periodical$tp + periodical$tn)
  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = nrow(online)
  df = data.frame(Time = 1:nrow(online), 
                  online = online$mc, 
                  continuous =  continuous$mc,
                  #baseline = baseline$mc,
                  periodical = append(periodical$mc,  maxLength))
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  df$online = df$online * 100
  df$continuous = df$continuous * 100
 # df$baseline = df$baseline * 100
  df$periodical = df$periodical * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

criteoDataProcessing <- function(){
  
}

taxiDataProcessing <- function(){
  
}


urlData = urlDataProcessing()
criteoData = urlDataProcessing()
taxiData = urlDataProcessing()

fontLabelSize = 10
baseSize = 14
breaks = c(1,2000,4000,6000, 8000, 10000, 12000)
labels = c("0","20","40", "60", "80", "100", "120")

url_plot = ggline(urlData, 'Time', 'value', ylab = "URL Misclassification (\\%)", xlab = 'Time (day)',
                  shape = '-1', linetype ='Deployment', ylim = c(2.07,2.4),size =1, color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels= labels)
url_plot = ggpar(url_plot, legend = "top", legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(plot.margin = unit(c(0,0,0,0), "lines"))

criteo_plot = ggline(criteoData, 'Time', 'value', ylab = "CTR MSE", xlab = 'Time (day)',
                  shape = '-1', linetype ='Deployment', ylim = c(2.07,2.4),size =1, color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels= labels) + rremove('legend')
criteo_plot = ggpar(criteo_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(plot.margin = unit(c(0,0,0,0), "lines"))

taxi_plot = ggline(taxiData, 'Time', 'value', ylab = "MSE", xlab = 'Time (day)',
                  shape = '-1', linetype ='Deployment', ylim = c(2.07,2.4),size =1, color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels= labels) + rremove('legend')
taxi_plot = ggpar(taxi_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + theme(plot.margin = unit(c(0,0,0,0), "lines"))

deployment_quality = ggarrange(url_plot,ggarrange(taxi_plot,criteo_plot, nrow = 1, ncol = 2), nrow = 2, ncol = 1, common.legend = FALSE) + rremove('legend') 

tikz(file = "../../images/experiment-results/tikz/deployment-quality-experiment.tex", width = 4, height = 4)
deployment_quality 
dev.off()
