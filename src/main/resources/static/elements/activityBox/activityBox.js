function ActivityBox(container_id,activityData) {
	// Parent constructor
	this.container = $(container_id);
	this.activity = activityData;
};

ActivityBox.prototype.draw = function() {
	this.container.load("/elements/activityBox/activityBox.html",this.activityBoxLoaded.bind(this));
}

ActivityBox.prototype.activityBoxLoaded = function() {
	var eventPretty = [];

	switch(this.activity.type) {
		case "CBTION":
			eventPretty = "contribution <a href=/views/cbtionPageR/"+this.activity.cbtionDto.id+">"+this.activity.cbtionDto.title+"</a> was "+this.activity.event;
			break;

		case "BID":
			switch(this.activity.event) {
				case "created":
					eventPretty = getUserPageLink(this.activity.bidDto.creatorDto.username)+
						" made a new bid on <a href=/views/cbtionPageR/"+this.activity.bidDto.cbtionId+">"+this.activity.bidDto.cbtionTitle+"</a>";
					break;

				case "assigned":
				case "accepted":
					
					eventPretty = "bid from "+getUserPageLink(this.activity.bidDto.creatorDto.username)+
						" to <a href=/views/cbtionPageR/"+this.activity.bidDto.cbtionId+">"+this.activity.bidDto.cbtionTitle+"</a> was "+this.activity.event;
					break;

				case "marked done":
					eventPretty = getUserPageLink(this.activity.bidDto.creatorDto.username)+
						" marked bid on <a href=/views/cbtionPageR/"+this.activity.bidDto.cbtionId+">"+this.activity.bidDto.cbtionTitle+"</a> as done";
					break;
			}
			
			break;

		case "GOAL":
			var goalLinkStr = getGoalPageLink(this.activity.goalDto.goalTag,this.activity.goalDto.projectName);
			switch(this.activity.event) {
				case "proposed":
					eventPretty = "goal "+goalLinkStr+" was "+this.activity.event+" by "+getUserPageLink(this.activity.goalDto.creatorUsername);
					break;
				default:
					eventPretty = "goal "+goalLinkStr+" - "+this.activity.event;
					break;
			}
			break;

		case "DECISION":
			switch(this.activity.event) {
				case "created":	
					eventPretty = "decision <a href=/views/decisionPageR/"+this.activity.decisionDto.id+">"+LimitStrSize(this.activity.decisionDto.description,70)+"</a> was "+this.activity.event+" by "+getUserPageLink(this.activity.decisionDto.creatorUsername);
					break;

				case "opened":
				case "accepted":
				case "rejected":
					eventPretty = "decision <a href=/views/decisionPageR/"+this.activity.decisionDto.id+">"+LimitStrSize(this.activity.decisionDto.description,70)+"</a> was "+this.activity.event;
					break;
			}
			break;

		case "ARGUMENT":
			switch(this.activity.event) {
				case "created":	
						eventPretty = getUserPageLink(this.activity.argumentDto.creatorUsername)+" added an argument to decision '<a href=DecisionPage?decisionId="+
						this.activity.argumentDto.decisionId+">"+LimitStrSize(this.activity.argumentDto.decisionDescription,70)+"</a>'";
						break;
			}
			break;
	}
	
	$("#activity_div",this.container).append("In "+getProjectLink(this.activity.projectName)+" "+getTimeStrSince(this.activity.creationDate)+" ago - "+eventPretty);
	
}
