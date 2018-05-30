setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/")
library(ggplot2)
library(reshape)
library(tikzDevice)
library(ggpubr)



urlDataProcessing <- function(){
  scale = 1000 * 60
  Default = sum(read.csv('url-reputation/final/optimization-effect/continuous-no-optimization-time_based-100/time', header = FALSE, col.names = c('time')))/scale
  Optimized = sum(read.csv('url-reputation/final/optimization-effect/continuous-with-optimization-time_based-100/time', header = FALSE, col.names = c('time')))/scale
  df = data.frame(Deployment = c('Default','Optimized'), 
                  Time = c(Default, Optimized))

  return (df)
}

taxiDataProcessing<- function(){
  scale = 1000 * 60
  Default = sum(read.csv('nyc-taxi/final/optimization-effect/continuous-no-optimization-time_based-720/time', header = FALSE, col.names = c('time')))/scale
  Optimized = sum(read.csv('nyc-taxi/final/optimization-effect/continuous-with-optimization-time_based-720/time', header = FALSE, col.names = c('time')))/scale
  df = data.frame(Deployment = c('Default','Optimized'), 
                  Time = c(Default, Optimized))
  
  
  return (df)
}

criteoDataProcessing <- function(){
  scale = 1000 * 60
  Default = sum(read.csv('url-reputation/final/optimization-effect/continuous-no-optimization-time_based-100/time', header = FALSE, col.names = c('time')))/scale
  Optimized = sum(read.csv('url-reputation/final/optimization-effect/continuous-with-optimization-time_based-100/time', header = FALSE, col.names = c('time')))/scale
  df = data.frame(Deployment = c('Default','Optimized'), 
                  Time = c(Default, Optimized))

  df$Time = 0
  return (df)
}

# For the paper use
# fontLabelSize = 12
# baseSize = 14
# margin = -3

# For presentation use
fontLabelSize = 16
baseSize = 18
margin = 2


####### URL PLOT ##########
urlData = urlDataProcessing()
urlPlot = ggbarplot(urlData, x = 'Deployment', y = 'Time',  ylab = 'Time (m)', xlab = "(a) URL",
                    width = 1.0, size = 1.0,
                    color = 'Deployment', fill = 'Deployment',
                    order = c('Optimized', 'Default'),
                    ggtheme = theme_pubclean(base_size = baseSize)) + rremove('x.ticks') + rremove('x.text') +
  theme(legend.key.width = unit(1.5,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        legend.title = element_text(size = 0),
        plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=margin)), 
        axis.title.x = element_text(margin = margin(t=margin)),
        legend.spacing.x = unit(-0.5, "cm"))
urlPlot = ggpar(urlPlot, legend.title = "", font.y=c(fontLabelSize), font.x=c(fontLabelSize+2)) + rremove('x.ticks') + rremove('x.text') + rremove("legend")

####### TAXI PLOT ##########
taxiData = taxiDataProcessing()
taxiPlot = ggbarplot(taxiData, x = 'Deployment', y = 'Time',  ylab = 'Time (m)', xlab = "(b) Taxi",
                     width = 1.0, size = 1.0,
                     color = 'Deployment', fill = 'Deployment',
                     order = c('Optimized', 'Default'),
                     ggtheme = theme_pubclean(base_size = baseSize)) + rremove('x.ticks') + rremove('x.text') +
  theme(legend.key.width = unit(1.5,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        legend.title = element_text(size = 0),
        plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=margin)),
        axis.title.x = element_text(margin = margin(t=margin)),
        legend.spacing.x = unit(-0.5, "cm")) 
taxiPlot = ggpar(taxiPlot, legend.title = "", font.y=c(fontLabelSize), font.x=c(fontLabelSize+2)) + rremove('x.ticks') + rremove('x.text')

####### CRITEO PLOT ##########
criteoData = criteoDataProcessing()
criteoPlot = ggbarplot(criteoData, x = 'Deployment', y = 'Time',  ylab = 'Time (m)', xlab = "(c) Criteo",
                       width = 1.0, size = 1.0,
                       color = 'Deployment', fill = 'Deployment',
                       order = c('Optimized', 'Default'),
                       ggtheme = theme_pubclean(base_size = baseSize),
                       ylim = c(0,max(urlData$Time)))+ rremove('x.ticks') + rremove('x.text') +
  theme(legend.key.width = unit(1.5,'cm'),
        legend.key.height = unit(0.4,'cm'), 
        legend.title = element_text(size = 0),
        plot.margin = unit(c(0,0,0,0), "lines"), 
        axis.title.y = element_text(margin = margin(r=margin)), 
        axis.title.x = element_text(margin = margin(t=margin)),
        legend.spacing.x = unit(-0.5, "cm"))
criteoPlot = ggpar(criteoPlot, font.y=c(fontLabelSize), font.x=c(fontLabelSize+2)) + rremove('x.ticks') + rremove('x.text')


#optimization_effects = ggarrange(urlPlot, taxiPlot, criteoPlot, nrow = 1, ncol = 3, common.legend = TRUE)

optimization_effects = ggarrange(urlPlot, taxiPlot, nrow = 1, ncol = 2, common.legend = TRUE)

ggsave(optimization_effects, filename = '../images/experiment-results/eps/optimization-time-experiment.eps', device = 'eps', width = 8, height = 4, units = "in")
#tikz(file = "../images/experiment-results/tikz/optimization-time-experiment.tex", width = 4, height = 2)
#optimization_effects 
#dev.off()


