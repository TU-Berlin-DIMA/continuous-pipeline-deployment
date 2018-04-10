setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)



urlDataProcessing <- function(){
  
  continuousNo = read.csv('optimization-effect/continuous-no-optimization-time', header = FALSE, col.names = c('time'))
  
  continuousYes = read.csv('optimization-effect/continuous-full-optimization-time', header = FALSE, col.names = c('time'))
  

  df = data.frame(Deployment = c('Default','Optimized'), 
                  Time = c(continuousNo$time, continuousYes$time))
  
  scale = 1000 * 60
  df$Time = df$Time / scale
  
  return (df)
}

fontLabelSize = 14
baseSize = 20
urlData = urlDataProcessing()
criteoData = urlDataProcessing()
taxiData = urlDataProcessing()
urlPlot = ggbarplot(urlData, x = 'Deployment', y = 'Time',  ylab = 'Time (m)', xlab = "(a) URL",
                    width = 1.0, size = 1.0,
                    color = 'Deployment', fill = 'Deployment',
                    order = c('Optimized', 'Default'),
                    ggtheme = theme_pubclean(base_size = baseSize)) + rremove('x.ticks') + rremove('x.text') +
  theme(legend.key.width = unit(1.5,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        legend.title = element_text(size = 0),
        plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-3)), 
        axis.title.x = element_text(margin = margin(t=-4)),
        legend.spacing.x = unit(-0.5, "cm"))
urlPlot = ggpar(urlPlot, font.y=c(fontLabelSize), font.x=c(fontLabelSize+2)) + rremove('x.ticks') + rremove('x.text') + rremove("legend")

taxiPlot = ggbarplot(taxiData, x = 'Deployment', y = 'Time',  ylab = 'Time (m)', xlab = "(b) Taxi",
                     width = 1.0, size = 1.0,
                     color = 'Deployment', fill = 'Deployment',
                     order = c('Optimized', 'Default'),
                     ggtheme = theme_pubclean(base_size = baseSize)) + rremove('x.ticks') + rremove('x.text') +
  theme(legend.key.width = unit(1.5,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        legend.title = element_text(size = 0),
        plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-3)),
        axis.title.x = element_text(margin = margin(t=-4)),
        legend.spacing.x = unit(-0.5, "cm")) 
taxiPlot = ggpar(taxiPlot, font.y=c(fontLabelSize), font.x=c(fontLabelSize+2)) + rremove('x.ticks') + rremove('x.text')

criteoPlot = ggbarplot(criteoData, x = 'Deployment', y = 'Time',  ylab = 'Time (m)', xlab = "(c) Criteo",
                       width = 1.0, size = 1.0,
                       color = 'Deployment', fill = 'Deployment',
                       order = c('Optimized', 'Default'),
                       ggtheme = theme_pubclean(base_size = baseSize))+ rremove('x.ticks') + rremove('x.text') +
  theme(legend.key.width = unit(1.5,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        legend.title = element_text(size = 0),
        plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=-3)), 
        axis.title.x = element_text(margin = margin(t=-4)),
        legend.spacing.x = unit(-0.5, "cm"))
criteoPlot = ggpar(criteoPlot, font.y=c(fontLabelSize), font.x=c(fontLabelSize+2)) + rremove('x.ticks') + rremove('x.text')


optimization_effects = ggarrange(urlPlot, taxiPlot, criteoPlot, nrow = 1, ncol = 3, common.legend = TRUE)

tikz(file = "../../images/experiment-results/tikz/optimization-time-experiment.tex", width = 6, height = 2)
optimization_effects 
dev.off()


