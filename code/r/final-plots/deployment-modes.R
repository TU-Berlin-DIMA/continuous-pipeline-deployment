setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)


urlDataProcessing <- function(){
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
urlLabels = c("day1","day30", "day60", "day90","day120")

taxiData = urlDataProcessing()
taxiBreaks = c(1,3000, 6000 ,9000, 12000)
taxiLabels = c("Feb15","Jul15", "Jan16", "Jul16", "Dec16")

criteoData = urlDataProcessing()
criteoBreaks = c(1,3000, 6000 ,9000, 12000)
criteoLabels = c("day1","day3", "day6", "day9","day12")



fontLabelSize = 10
baseSize = 14


urlPlot = ggline(urlData, 'Time', 'value', ylab = "Misclassification (\\%)", xlab = '(a) URL',
                  shape = '-1', size = 1, linetype ='Deployment', color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks, labels= urlLabels)
urlPlot = ggpar(urlPlot, legend = "top", legend.title = "", font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))


taxiPlot = ggline(taxiData, 'Time', 'value', ylab = "RMSLE", xlab = '(b) Taxi',
                  shape = '-1',size =1, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks, labels= taxiLabels) + rremove('legend')
taxiPlot = ggpar(taxiPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

criteoPlot = ggline(criteoData, 'Time', 'value', ylab = "MSE", xlab = '(c) Criteo',
                     shape = '-1',size =1, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = criteoBreaks, labels= criteoLabels) + rremove('legend')
criteoPlot = ggpar(criteoPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) + 
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

deploymentQuality = ggarrange(urlPlot,taxiPlot,criteoPlot, nrow = 3, ncol = 1, common.legend = TRUE)

tikz(file = "../images/experiment-results/tikz/deployment-quality-experiment.tex", width = 4, height = 4)
deploymentQuality 
dev.off()
