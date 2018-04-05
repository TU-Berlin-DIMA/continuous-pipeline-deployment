setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)


urlDataProcessing <- function(){
  weighted = cumsum(read.csv('sampling-modes/confusion_matrix-time_based-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  weighted$mc = (weighted$fp + weighted$fn) / (weighted$fp + weighted$fn + weighted$tp + weighted$tn)
  window = cumsum(read.csv('sampling-modes/confusion_matrix-window(1000)-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  window$mc = (window$fp + window$fn) / (window$fp + window$fn + window$tp + window$tn)
  uniform = cumsum(read.csv('sampling-modes/confusion_matrix-uniform-100', header = FALSE, col.names = c('tp','fp','tn','fn')))
  uniform$mc = (uniform$fp + uniform$fn) / (uniform$fp + uniform$fn + uniform$tp + uniform$tn)
  
  append <- function(vec, maxLength){
    return (c(vec,rep(NA, maxLength - length(vec))))
  }
  maxLength = nrow(uniform)
  
  df = data.frame(Time = 1:nrow(uniform),
                  weighted = weighted$mc,
                  window = window$mc, 
                  uniform = uniform$mc)
  
  DAY_DURATION = 100
  df = df[((df$Time %% DAY_DURATION == 0) | df$Time == 200), ]
  df$weighted = df$weighted * 100
  df$window = df$window * 100
  df$uniform = df$uniform * 100
  ml = melt(df, id.vars = 'Time', variable_name ='Sampling')
  return (ml)
}

criteoDataProcessing <- function(){
  
}

taxiDataProcessing <- function(){
  
}

urlData = urlDataProcessing()
criteoData = urlDataProcessing()
taxiData = urlDataProcessing()

fontLabelSize = 14
baseSize = 20
breaks = c(1,200,400,600, 800, 1000)
labels = c("0","2","4", "6", "8", "10")

url_plot = ggline(urlData, 'Time', 'value', ylab = "Misclass (\\%)", xlab = 'Day',
                  shape = '-1', linetype ='Sampling', size =2, color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels= labels)
url_plot = ggpar(url_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize), legend.title = NULL) +
  theme(plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-3)),
        axis.title.x = element_text(margin = margin(t=-2)),
        axis.text.x = element_text(margin = margin(t=-3)))
url_plot
criteo_plot = ggline(criteoData, 'Time', 'value', ylab = "MSE", xlab = 'Day',
                     shape = '-1', linetype ='Sampling', size =2, color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels= labels)
criteo_plot = ggpar(criteo_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize), legend.title = NULL) +
  theme(plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-3)),
        axis.title.x = element_text(margin = margin(t=-2)),
        axis.text.x = element_text(margin = margin(t=-3)))

taxi_plot = ggline(taxiData, 'Time', 'value', ylab = "MSE", xlab = 'Day',
                   shape = '-1', linetype ='Sampling',size = 2, color = "Sampling", ggtheme = theme_pubclean(base_size = baseSize)) + 
  scale_x_continuous(breaks = breaks, labels = labels)
taxi_plot = ggpar(taxi_plot, font.x = c(fontLabelSize), font.y=c(fontLabelSize), legend.title = NULL)+
  theme(plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-3)),
        axis.title.x = element_text(margin = margin(t=-2)),
        axis.text.x = element_text(margin = margin(t=-3)))


sampling_plot = ggarrange(url_plot, taxi_plot,criteo_plot,  nrow = 1, ncol = 3, common.legend = TRUE) + theme(legend.title = NULL)
tikz(file = "../../images/experiment-results/tikz/sampling-mode-figure.tex", width = 6, height = 2)
sampling_plot
dev.off()
