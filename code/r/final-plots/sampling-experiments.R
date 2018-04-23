setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)


urlDataProcessing <- function(){
  getMisclassification <-function(loc){
    confusionMatrix = cumsum(read.csv(loc, header = FALSE, col.names = c('tp','fp','tn','fn')))
    return((confusionMatrix$fp + confusionMatrix$fn) / (confusionMatrix$fp + confusionMatrix$fn + confusionMatrix$tp + confusionMatrix$tn))
  }
  timeBased = getMisclassification('url-reputation/sampling-modes/confusion_matrix-time_based-100')
  windowBased = getMisclassification('url-reputation/sampling-modes/confusion_matrix-window(1000)-100')
  uniform = getMisclassification('url-reputation/sampling-modes/confusion_matrix-uniform-100')
  
  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = length(uniform)
  
  df = data.frame(Time = 1:length(uniform),
                  Timebased = append(timeBased,maxLength),
                  Windowbased = windowBased, 
                  Uniform = uniform)
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 100), ]

  df[,c(2,3,4)] = df[,c(2,3,4)] * 100
  
  ml = melt(df, id.vars = 'Time', variable_name ='Sampling')
  return (ml)
}

criteoDataProcessing <- function(){
  getMisclassification <-function(loc){
    confusionMatrix = cumsum(read.csv(loc, header = FALSE, col.names = c('tp','fp','tn','fn')))
    return((confusionMatrix$fp + confusionMatrix$fn) / (confusionMatrix$fp + confusionMatrix$fn + confusionMatrix$tp + confusionMatrix$tn))
  }
  timeBased = getMisclassification('url-reputation/sampling-modes/confusion_matrix-time_based-100')
  windowBased = getMisclassification('url-reputation/sampling-modes/confusion_matrix-window(1000)-100')
  uniform = getMisclassification('url-reputation/sampling-modes/confusion_matrix-uniform-100')
  
  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = length(uniform)
  
  df = data.frame(Time = 1:length(uniform),
                  Timebased = append(timeBased,maxLength),
                  Windowbased = windowBased, 
                  Uniform = uniform)
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 100), ]
  
  df[,c(2,3,4)] = df[,c(2,3,4)] * 100
  
  df[,c(2,3,4)]  = 0
  
  ml = melt(df, id.vars = 'Time', variable_name ='Sampling')
  return (ml)
}

taxiDataProcessing <- function(){
  getMisclassification <-function(loc){
    confusionMatrix = cumsum(read.csv(loc, header = FALSE, col.names = c('tp','fp','tn','fn')))
    return((confusionMatrix$fp + confusionMatrix$fn) / (confusionMatrix$fp + confusionMatrix$fn + confusionMatrix$tp + confusionMatrix$tn))
  }
  timeBased = getMisclassification('url-reputation/sampling-modes/confusion_matrix-time_based-100')
  windowBased = getMisclassification('url-reputation/sampling-modes/confusion_matrix-window(1000)-100')
  uniform = getMisclassification('url-reputation/sampling-modes/confusion_matrix-uniform-100')
  
  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = length(uniform)
  
  df = data.frame(Time = 1:length(uniform),
                  Timebased = append(timeBased,maxLength),
                  Windowbased = windowBased, 
                  Uniform = uniform)
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 100), ]
  
  df[,c(2,3,4)] = df[,c(2,3,4)] * 100
  
  df[,c(2,3,4)]  = 0
  
  ml = melt(df, id.vars = 'Time', variable_name ='Sampling')
  return (ml)
}

urlData = urlDataProcessing()
urlBreaks = c(100, 1500,3000)
urlLabels = c("day1","day15","day30")

taxiData = taxiDataProcessing()
taxiBreaks = c(100,1500,3000)
taxiLabels = c("Feb15", "Mar15", "Apr15")

criteoData = criteoDataProcessing()
criteoBreaks = c(100,1500,3000)
criteoLabels = c("day1", "day3","day6")

fontLabelSize = 14
baseSize = 20

urlPlot = ggline(urlData, 'Time', 'value', ylab = "Misclassification\\%", xlab = '(a) URL',
                 shape = '-1', linetype ='Sampling', size =2, color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = urlBreaks, labels= urlLabels)
urlPlot = ggpar(urlPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) +
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.2,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

taxiPlot = ggline(taxiData, 'Time', 'value', ylab = "RMSLE", xlab = '(b) Taxi',
                  shape = '-1', linetype ='Sampling',size = 2, color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize),
                  ylim=c(min(urlData$value), max(urlData$value))) + 
  scale_x_continuous(breaks = taxiBreaks, labels = taxiLabels)
taxiPlot = ggpar(taxiPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize))+
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

criteoPlot = ggline(criteoData, 'Time', 'value', ylab = "MSE", xlab = '(c) Criteo',
                    shape = '-1', linetype ='Sampling', size =2, color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize),
                    ylim=c(min(urlData$value), max(urlData$value))) + 
  scale_x_continuous(breaks = criteoBreaks, labels= criteoLabels)
criteoPlot = ggpar(criteoPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) +
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))


samplingPlot = ggarrange(urlPlot, taxiPlot,criteoPlot,  nrow = 1, ncol = 3, common.legend = TRUE)

tikz(file = "../images/experiment-results/tikz/sampling-mode-figure.tex", width = 6, height = 2)
samplingPlot
dev.off()
