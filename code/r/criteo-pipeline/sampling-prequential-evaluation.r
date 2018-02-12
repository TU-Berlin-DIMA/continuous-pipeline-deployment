setwd("~/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/")
library(ggplot2)
library(reshape)


# cluster
entireHistory = read.csv('sampling-mode/cluster-prequential/continuous/loss_-1', header = FALSE, col.names = c('entireHistory'))
oneDay = read.csv('sampling-mode/cluster-prequential/continuous/loss_1440', header = FALSE, col.names = c('oneDay'))
halfDay = read.csv('sampling-mode/cluster-prequential/continuous/loss_720', header = FALSE, col.names = c('halfDay'))
noSampling = read.csv('sampling-mode/cluster-prequential/continuous/loss_0', header = FALSE, col.names = c('noSampling'))
breaks = c(1,1441,2881,4321,5761,7201)
labels = c("Deployment","Day 1","Day 2", "Day 3", "Day 4", "Day 5")

plotEvalResult <- function(df, tite){
  groupColors <- c(entireHistory = "#d11141", oneDay = "#00b159", halfDay ="#00aedb", noSampling = "#f37735")
  groupNames <- c(entireHistory = "Entire History", oneDay = "One Day", halfDay ="Half Day", noSampling = "No Sampling")
  ml = melt(df, id.vars = 'time')
  pl = 
    ggplot(data = ml, aes(x = time, y = value, group = variable)) + 
    geom_line(aes( colour = variable), size = 0.6) + 
    ylab("Logistic Loss") + 
    labs(title = tite) +
    theme_bw() + 
    scale_x_continuous(name ="Time",
                       breaks = breaks,
                       labels= labels) +
    scale_color_manual(values = groupColors, 
                       labels = groupNames,
                       guide = guide_legend(override.aes = list(shape = c(NA)))) +
    theme(legend.text = element_text(size = 30, color = "black"), 
          axis.text=element_text(size=30, color = "black"),
          axis.title=element_text(size=30, color= "black"),
          legend.key.width = unit(2, "cm"), 
          legend.key.height = unit(0.8, "cm"),
          legend.position = "bottom",
          legend.title = element_blank(),
          panel.border = element_rect(colour = "black", fill=NA, size=3),
          plot.margin=unit(c(0.1,1,0.1,0.1),"cm"),
          title = element_text(size=20, color = "black", hjust  = 0.5))
  return (pl)
}

append <- function(vec, maxLength){
  return (c(vec,rep(NA, maxLength - length(vec))))
}
maxLength = nrow(noSampling)
df = data.frame(time = 1:maxLength,
                entireHistory = append(entireHistory$entireHistory, maxLength),
                oneDay = append(oneDay$oneDay, maxLength),
                halfDay = append(halfDay$halfDay, maxLength), 
                noSampling = noSampling)

hourly = df[df$time %% 60 == 0,]

hourly_plot = plotEvalResult(hourly, "Hourly Evaluation")
ggsave(hourly_plot , filename = 'sampling-mode/cluster-prequential/continuous/figures/hourly.eps', device = 'eps', width = 12, height = 6, units = "in")

cum_beginning_of_time = df
cum_beginning_of_time$halfDay = cumsum(df$halfDay)/df$time
cum_beginning_of_time$oneDay = cumsum(df$oneDay)/df$time
cum_beginning_of_time$entireHistory = cumsum(df$entireHistory)/df$time
cum_beginning_of_time$noSampling = cumsum(df$noSampling)/df$time
cum_beginning_of_time_plot = plotEvalResult(cum_beginning_of_time, "Cumulative Average Evaluation")
ggsave(cum_beginning_of_time_plot , filename = 'sampling-mode/cluster-prequential/continuous/figures/cum-average.eps', device = 'eps', width = 12, height = 6, units = "in")


hourly_cum_beginning_average = cum_beginning_of_time[cum_beginning_of_time$time %% 60 == 0,]
hourly_cum_beginning_of_time_plot = plotEvalResult(hourly_cum_beginning_average, "Cumulative Average Evaluation (reported hourly")
ggsave(hourly_cum_beginning_of_time_plot , filename = 'sampling-mode/cluster-prequential/continuous/figures/hourly-cum-average.eps', device = 'eps', width = 12, height = 6, units = "in")

library(zoo)
rolling_average_hourly

roll_entireHistory = rollmean(df$entireHistory, 60)
roll_oneDay = rollmean(df$oneDay, 60)
roll_halfDay = rollmean(df$halfDay, 60)
roll_time = 1:length(roll_halfDay)
rolling_average = data.frame(time = roll_time,
                             entireHistory = roll_entireHistory,
                             oneDay = roll_oneDay,
                             halfDay = roll_halfDay)

rolling_average_plot = plotEvalResult(rolling_average, "Rolling Average Evaluation")
ggsave(rolling_average_plot , filename = 'sampling-mode/cluster-prequential/continuous/figures/rolling-average.eps', device = 'eps', width = 12, height = 6, units = "in")


hourly_rolling_average = rolling_average[rolling_average$time %% 60 == 0,]
hourly_rolling_average_plot = plotEvalResult(hourly_rolling_average, "Hourly Rolling Average Evaluation")
ggsave(hourly_rolling_average_plot , filename = 'sampling-mode/cluster-prequential/continuous/figures/hourly-rolling-average.eps', device = 'eps', width = 12, height = 6, units = "in")
