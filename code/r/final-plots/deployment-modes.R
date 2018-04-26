setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)
source('../code/r/final-plots/functions.r')

urlDataProcessing <- function(){
  Online = getMisclassification('url-reputation/deployment-modes-quality-time/confusion_matrix-online')
  Continuous = getMisclassification('url-reputation/deployment-modes-quality-time/confusion_matrix-time_based-100-with-optimization')
  Baseline = getMisclassification('url-reputation/deployment-modes-quality-time/confusion_matrix-baseline')
  periodical = getMisclassification('url-reputation/deployment-modes-quality-time/confusion_matrix-periodical-warm')
 
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = length(periodical)
  df = data.frame(Time = 1:length(Periodical), Continuous, Periodical, Online)
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  df$Online = df$Online * 100
  df$Continuous = df$Continuous * 100
  df$Periodical = df$Periodical * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

criteoDataProcessing <- function(){
  Online = getMisclassification('url-reputation/deployment-modes-quality-time/confusion_matrix-online')
  Continuous = getMisclassification('url-reputation/deployment-modes-quality-time/confusion_matrix-time_based-100-with-optimization')
  Baseline = getMisclassification('url-reputation/deployment-modes-quality-time/confusion_matrix-baseline')
  periodical = getMisclassification('url-reputation/deployment-modes-quality-time/confusion_matrix-periodical-warm')
  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = length(periodical)
  df = data.frame(Time = 1:length(Periodical), Continuous, Periodical, Online)
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  df$Online = df$Online * 100
  df$Continuous = df$Continuous * 100
  df$Periodical = df$Periodical * 100
  df[,-1] = 0
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}

taxiDataProcessing <- function(){
  Online = getRMSLE('nyc-taxi/deployment-modes/online/rmsle')
  Continuous = getRMSLE('nyc-taxi/deployment-modes/continuous-with-optimization-time_based-720/rmsle')
  #Baseline = getRMSLE('nyc-taxi/deployment-modes/continuous-with-optimization-time_based-720/rmsle')
  #periodical = getRMSLE('nyc-taxi/deployment-modes/continuous-with-optimization-time_based-720/rmsle')

  maxLength = length(Online)
  df = data.frame(Time = 1:length(Online), append(Continuous,maxLength), Online)
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Deployment')
  return(ml)
}


urlData = urlDataProcessing()
urlBreaks = c(1,3000, 6000 ,9000, 12000)
urlLabels = c("day1","day30", "day60", "day90","day120")

taxiData = taxiDataProcessing()
taxiBreaks = c(1,4000, 8000, 12000)
taxiLabels = c("Feb15","Jul15", "Jan16", "June16")

criteoData = criteoDataProcessing()
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
                     shape = '-1',size =1, linetype ='Deployment',color = "Deployment", ggtheme = theme_pubclean(base_size = baseSize),
                    ylim = c(min(urlData$value), max(urlData$value))) +  
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
