getMisclassification <-function(loc){
  confusionMatrix = cumsum(read.csv(loc, header = FALSE, col.names = c('tp','fp','tn','fn')))
  return((confusionMatrix$fp + confusionMatrix$fn) / (confusionMatrix$fp + confusionMatrix$fn + confusionMatrix$tp + confusionMatrix$tn))
}

getRMSLE <-function(loc){
  rmsle = cumsum(read.csv(loc, header = FALSE, col.names = c('ssl','count')))
  return(sqrt(rmsle$ssl/rmsle$count))
}

getLogisticLoss <-function(loc){
  ls = cumsum(read.csv(loc, header = FALSE, col.names = c('loss','count')))
  return(ls$loss/ls$count)
}

append <- function(vec, maxLength){
  return (c(vec,rep(NA, maxLength - length(vec))))
}