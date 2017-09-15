package org.collectiveone.modules.initiatives;

import java.util.List;
import java.util.UUID;

import org.collectiveone.common.BaseController;
import org.collectiveone.common.dto.GetResult;
import org.collectiveone.common.dto.PostResult;
import org.collectiveone.modules.governance.DecisionMakerRole;
import org.collectiveone.modules.governance.DecisionVerdict;
import org.collectiveone.modules.governance.GovernanceService;
import org.collectiveone.modules.initiatives.dto.InitiativeDto;
import org.collectiveone.modules.initiatives.dto.InitiativeTagDto;
import org.collectiveone.modules.initiatives.dto.MemberDto;
import org.collectiveone.modules.initiatives.dto.NewInitiativeDto;
import org.collectiveone.modules.initiatives.dto.SearchFiltersDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/1")
public class InitiativesController extends BaseController { 
	
	@Autowired
	private GovernanceService governanceService;
	
	@Autowired
	private InitiativeService initiativeService;
	
	
	@RequestMapping(path = "/initiative", method = RequestMethod.POST)
	public PostResult createInitiative(@RequestBody NewInitiativeDto initiativeDto) {
		
		if (getLoggedUser() == null) {
			return new PostResult("error", "endpoint enabled users only", null);
		}
		
		/* Authorization is needed if it is a subinitiative */
		DecisionVerdict canCreate = null;
		
		if (!initiativeDto.getAsSubinitiative()) {
			canCreate = DecisionVerdict.APPROVED;
		} else {
			canCreate = governanceService.canCreateSubInitiative(UUID.fromString(initiativeDto.getParentInitiativeId()), getLoggedUser().getC1Id());
		}
		
		if (canCreate == DecisionVerdict.DENIED) {
			return new PostResult("error", "creation not of subinitiative not authorized",  "");
		}
				
		return initiativeService.init(getLoggedUser().getC1Id(), initiativeDto);
	}
	
	@RequestMapping(path = "/initiative/{initiativeId}", method = RequestMethod.PUT)
	public PostResult editInitiative(@PathVariable("initiativeId") String initiativeIdStr, @RequestBody NewInitiativeDto initiativeDto) {
		
		if (getLoggedUser() == null) {
			return new PostResult("error", "endpoint enabled users only", null);
		}
		
		UUID initiativeId = UUID.fromString(initiativeIdStr);
		if (governanceService.canEdit(initiativeId, getLoggedUser().getC1Id()) == DecisionVerdict.DENIED) {
			return new PostResult("error", "not authorized", "");
		}
			
		return initiativeService.edit(initiativeId, getLoggedUser().getC1Id(), initiativeDto); 
	}
	
	@RequestMapping(path = "/initiative/{initiativeId}", method = RequestMethod.DELETE)
	public PostResult deleteInitiative(@PathVariable("initiativeId") String initiativeIdStr) {
		
		if (getLoggedUser() == null) {
			return new PostResult("error", "endpoint enabled users only", null);
		}
		
		UUID initiativeId = UUID.fromString(initiativeIdStr);
		
		DecisionVerdict canRevert = governanceService.canDeleteInitiative(initiativeId, getLoggedUser().getC1Id());
		
		if (canRevert == DecisionVerdict.DENIED) {
			return new PostResult("error", "revert of assignation not authorized",  "");
		}
		
		return initiativeService.delete(initiativeId, getLoggedUser().getC1Id());
		
	}
	
	
	@RequestMapping(path = "/initiative/{initiativeId}", method = RequestMethod.GET)
	public GetResult<InitiativeDto> getInitiative(
			@PathVariable("initiativeId") String initiativeId, 
			@RequestParam(defaultValue = "false") boolean addAssets,
			@RequestParam(defaultValue = "false") boolean addSubinitiatives,
			@RequestParam(defaultValue = "false") boolean addParents,
			@RequestParam(defaultValue = "false") boolean addMembers,
			@RequestParam(defaultValue = "false") boolean addLoggedUser) {
		
		InitiativeDto initiativeDto = null;
		
		initiativeDto = initiativeService.getLight(UUID.fromString(initiativeId));
		
		if(addAssets) {
			initiativeDto.setAssets(initiativeService.getInitiativeAssets(UUID.fromString(initiativeId)));
		}
		
		if(addSubinitiatives) {
			initiativeDto.setSubInitiatives(initiativeService.getSubinitiativesTree(UUID.fromString(initiativeId), null));
		}
		
		if(addParents) {
			initiativeDto.setParents(initiativeService.getParentInitiativesDtos(UUID.fromString(initiativeId)));
		}
		
		if(addMembers) {
			initiativeDto.setInitiativeMembers(initiativeService.getMembersAndSubmembers(UUID.fromString(initiativeId)));
		}
		
		if(addLoggedUser) {
			initiativeDto.setLoggedMember(initiativeService.getMember(UUID.fromString(initiativeId),  getLoggedUserId()));
		}
		
		return new GetResult<InitiativeDto>("success", "initiative retrieved", initiativeDto);
	}
	
	@RequestMapping(path = "/initiatives/mines", method = RequestMethod.GET)
	public GetResult<List<InitiativeDto>> myInitiatives() {
		if (getLoggedUser() == null) {
			return new GetResult<List<InitiativeDto>>("error", "endpoint enabled users only", null);
		}
		
		return initiativeService.getOfUser(getLoggedUser().getC1Id());
	}
	
	@RequestMapping(path = "/initiatives/search", method = RequestMethod.PUT)
	public GetResult<List<InitiativeDto>> search(@RequestBody SearchFiltersDto searchFilters) {
		return initiativeService.searchBy(searchFilters);
	}
	
	@RequestMapping(path = "/initiative/{initiativeId}/member", method = RequestMethod.POST) 
	public PostResult addMember(@PathVariable("initiativeId") String initiativeId, @RequestBody MemberDto memberDto) {
		
		if (getLoggedUser() == null) {
			return new PostResult("error", "endpoint enabled users only", null);
		}
		
		DecisionVerdict verdict = governanceService.canAddMember(UUID.fromString(initiativeId), getLoggedUser().getC1Id());
		
		if (verdict == DecisionVerdict.DENIED) {
			return new PostResult("error", "not authorized", "");
		} 
		
		return initiativeService.postMember(
				UUID.fromString(initiativeId), 
				UUID.fromString(memberDto.getUser().getC1Id()),
				DecisionMakerRole.valueOf(memberDto.getRole()));
	}
	
	@RequestMapping(path = "/initiative/{initiativeId}/member/{userId}", method = RequestMethod.DELETE) 
	public PostResult deleteMember(@PathVariable("initiativeId") String initiativeId, @PathVariable("userId") String userId) {
		
		if (getLoggedUser() == null) {
			return new PostResult("error", "endpoint enabled users only", null);
		}
		
		DecisionVerdict verdict = governanceService.canDeleteMember(UUID.fromString(initiativeId), getLoggedUser().getC1Id());
		
		if (verdict == DecisionVerdict.DENIED) {
			return new PostResult("error", "not authorized", "");
		} 
		
		return initiativeService.deleteMember(UUID.fromString(initiativeId), UUID.fromString(userId));
	}
	
	@RequestMapping(path = "/initiative/{initiativeId}/member/{userId}", method = RequestMethod.PUT) 
	public PostResult editMember(@PathVariable("initiativeId") String initiativeId, @RequestBody MemberDto memberDto) {
		
		if (getLoggedUser() == null) {
			return new PostResult("error", "endpoint enabled users only", null);
		}
		
		DecisionVerdict verdict = governanceService.canAddMember(UUID.fromString(initiativeId), getLoggedUser().getC1Id());
		
		if (verdict == DecisionVerdict.DENIED) {
			return new PostResult("error", "not authorized", "");
		} 
		
		return initiativeService.editMember(
				UUID.fromString(initiativeId), 
				UUID.fromString(memberDto.getUser().getC1Id()),
				DecisionMakerRole.valueOf(memberDto.getRole()));
	}
	
	@RequestMapping(path = "/initiative/{initiativeId}/tags", method = RequestMethod.POST) 
	public PostResult addTagToInitiative(
			@PathVariable("initiativeId") String initiativeIdStr, @RequestBody InitiativeTagDto tagDto) {
		
		if (getLoggedUser() == null) {
			return new PostResult("error", "endpoint enabled users only", null);
		}
		
		UUID initiativeId = UUID.fromString(initiativeIdStr);
		DecisionVerdict verdict = governanceService.canEdit(initiativeId, getLoggedUser().getC1Id());
		
		if (verdict == DecisionVerdict.DENIED) {
			return new PostResult("error", "not authorized", "");
		} 
		
		return initiativeService.addTagToInitiative(initiativeId, tagDto);
	}
	
	@RequestMapping(path = "/initiative/{initiativeId}/tags/{tagId}", method = RequestMethod.DELETE) 
	public PostResult deleteTagFromInitiative(
			@PathVariable("initiativeId") String initiativeIdStr,
			@PathVariable("tagId") String tagIdStr) {
		
		if (getLoggedUser() == null) {
			return new PostResult("error", "endpoint enabled users only", null);
		}
		
		UUID initiativeId = UUID.fromString(initiativeIdStr);
		DecisionVerdict verdict = governanceService.canEdit(initiativeId, getLoggedUser().getC1Id());
		
		if (verdict == DecisionVerdict.DENIED) {
			return new PostResult("error", "not authorized", "");
		} 
		
		return initiativeService.deleteTagFromInitiative(initiativeId, UUID.fromString(tagIdStr));
	}
	
	@RequestMapping(path = "/initiative/tags/suggestions", method = RequestMethod.GET)
	public GetResult<List<InitiativeTagDto>> tagSuggestions(@RequestParam("q") String query) {
		return initiativeService.searchTagsBy(query);
	}
	
	@RequestMapping(path = "/initiative/tag/{tagId}", method = RequestMethod.GET)
	public GetResult<InitiativeTagDto> getTag(@PathVariable("tagId") String tagId) {
		return initiativeService.getTag(UUID.fromString(tagId));
	}
	
	@RequestMapping(path = "/initiative/tag", method = RequestMethod.POST)
	public PostResult newTag(@RequestBody InitiativeTagDto tagDto) {
		
		if (getLoggedUser() == null) {
			return new PostResult("error", "endpoint enabled users only", null);
		}
		
		InitiativeTag tag = initiativeService.getOrCreateTag(tagDto);
		return new PostResult("success", "tag craeted", tag.getId().toString());
	}
	
}
