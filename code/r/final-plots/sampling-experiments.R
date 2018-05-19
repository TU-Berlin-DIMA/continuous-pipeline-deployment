setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)
source('../code/r/final-plots/functions.r')

urlDataProcessing <- function(){
  timeBased = getMisclassification('url-reputation/final/sampling-modes/continuous-with-optimization-time_based-100/confusion_matrix')
  windowBased = getMisclassification('url-reputation/final/sampling-modes/continuous-with-optimization-window(1000)-100/confusion_matrix')
  uniform = getMisclassification('url-reputation/final/sampling-modes/continuous-with-optimization-uniform-100/confusion_matrix')
  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = length(uniform)
  df = data.frame(Time = 1:length(uniform),
                  Timebased = append(timeBased,maxLength),
                  Windowbased = windowBased, 
                  Uniform = uniform)
  
  DAY_DURATION = 500
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 200), ]

  df[,c(2,3,4)] = df[,c(2,3,4)] * 100
  
  ml = melt(df, id.vars = 'Time', variable_name ='Sampling')
  return (ml)
}

criteoDataProcessing <- function(){
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
  Timebased = getRMSLE('nyc-taxi/final/sampling-modes/continuous-with-optimization-time_based-720/rmsle')
  WindowBased = getRMSLE('nyc-taxi/final/sampling-modes/continuous-with-optimization-window(7200)-720/rmsle')
  Uniform = getRMSLE('nyc-taxi/final/sampling-modes/continuous-with-optimization-uniform-720/rmsle')
  
  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = length(Uniform)
  
  df = data.frame(Time = 1:length(Uniform), Timebased, WindowBased, Uniform)
                  
  
  DAY_DURATION = 250
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 1), ]
  ml = melt(df, id.vars = 'Time', variable_name ='Sampling')
  return (ml)
}

fontLabelSize = 12
baseSize = 14

####### URL PLOT ##########
urlData = urlDataProcessing()
urlBreaks = c(200, 1500,3000)
urlLabels = c("day1","day15","day30")
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

####### TAXI PLOT ##########
taxiData = taxiDataProcessing()
taxiBreaks = c(1,2000)
taxiLabels = c("Feb15", "Apr15")
taxiPlot = ggline(taxiData, 'Time', 'value', ylab = "RMSLE", xlab = '(b) Taxi',
                  shape = '-1', linetype ='Sampling',size = 2, color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = taxiBreaks, labels = taxiLabels)
taxiPlot = ggpar(taxiPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize))+
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1.5,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))

####### CRITEO PLOT ##########
criteoData = urlData#criteoDataProcessing()
criteoBreaks = c(200, 1500,3000)
criteoLabels = c("day1", "day3","day6")
criteoPlot = ggline(criteoData, 'Time', 'value', ylab = "MSE", xlab = '(c) Criteo',
                    shape = '-1', linetype ='Sampling', size =0, color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize),
                    ylim=c(min(urlData$value), max(urlData$value))) + 
  scale_x_continuous(breaks = criteoBreaks, labels= criteoLabels)
criteoPlot = ggpar(criteoPlot, font.x = c(fontLabelSize), font.y=c(fontLabelSize)) +
  theme(legend.title = element_text(size = 0), 
        legend.key.width = unit(1.2,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        plot.margin = unit(c(0,1,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-1)),
        axis.text.x = element_text(margin = margin(t=-1)))


#samplingPlot = ggarrange(urlPlot, taxiPlot,criteoPlot,  nrow = 1, ncol = 3, common.legend = TRUE)

samplingPlot = ggarrange(urlPlot, taxiPlot,  nrow = 1, ncol = 2, common.legend = TRUE)

tikz(file = "../images/experiment-results/tikz/sampling-mode-figure.tex", width = 4, height = 2)
samplingPlot
dev.off()
